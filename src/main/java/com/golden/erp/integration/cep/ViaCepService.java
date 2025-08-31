package com.golden.erp.integration.cep;

import com.golden.erp.dto.cliente.EnderecoRequestDTO;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class ViaCepService {

    private static final Logger logger = LoggerFactory.getLogger(ViaCepService.class);

    private final ViaCepClient viaCepClient;
    private final ApplicationContext context; // Spring context

    public ViaCepService(ViaCepClient viaCepClient, ApplicationContext context) {
        this.viaCepClient = viaCepClient;
        this.context = context;
    }

    /**
     * Preenche os campos vazios do endereço consultando a API ViaCEP
     */
    public EnderecoRequestDTO preencherEnderecoComViaCep(EnderecoRequestDTO enderecoRequest) {
        if (enderecoRequest.getCep() == null || enderecoRequest.getCep().trim().isEmpty()) {
            logger.debug("CEP não informado, retornando endereço sem alterações");
            return enderecoRequest;
        }

        // Se todos os campos obrigatórios já estão preenchidos, não consulta o ViaCEP
        if (camposEnderecoCompletos(enderecoRequest)) {
            logger.debug("Endereço já completo, não será consultado o ViaCEP");
            return enderecoRequest;
        }

        // Chamada através do proxy do Spring para habilitar retry
        ViaCepService selfProxy = context.getBean(ViaCepService.class);
        return selfProxy.consultarViaCep(enderecoRequest);
    }

    /**
     * Consulta ViaCEP com retry automático para erros de servidor (5xx)
     * Usa backoff exponencial: 1s, 2s, 4s
     *
     * Método público para permitir que o proxy do Spring intercepte corretamente
     * as chamadas e aplique o retry conforme configurado.
     */
    @Retryable(
            retryFor = {FeignException.class},
            noRetryFor = {FeignException.BadRequest.class, FeignException.NotFound.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public EnderecoRequestDTO consultarViaCep(EnderecoRequestDTO enderecoRequest) {
        logger.info("Consultando ViaCEP para CEP: {}", enderecoRequest.getCep());

        try {
            ViaCepResponseDTO viaCepResponse = viaCepClient.consultarCep(enderecoRequest.getCep());

            if (viaCepResponse.getErro() != null && viaCepResponse.getErro()) {
                logger.warn("ViaCEP retornou erro para CEP: {}", enderecoRequest.getCep());
                throw new RuntimeException("CEP inválido: " + enderecoRequest.getCep());
            }

            logger.info("Dados do ViaCEP obtidos com sucesso para CEP: {}", enderecoRequest.getCep());
            return preencherCamposVazios(enderecoRequest, viaCepResponse);

        } catch (FeignException.BadRequest e) {
            logger.error("CEP inválido (400): {}", enderecoRequest.getCep());
            throw new RuntimeException("CEP inválido: " + enderecoRequest.getCep());
        } catch (FeignException.NotFound e) {
            logger.error("CEP não encontrado (404): {}", enderecoRequest.getCep());
            throw new RuntimeException("CEP não encontrado: " + enderecoRequest.getCep());
        } catch (FeignException e) {
            logger.error("Erro ao consultar ViaCEP para CEP {}: {} (Status: {})",
                    enderecoRequest.getCep(), e.getMessage(), e.status());

            if (e.status() >= 500) {
                // Para erros 5xx, o retry será automático via @Retryable
                throw e;
            } else {
                // Para outros erros 4xx não tratados especificamente, não faz retry
                throw new RuntimeException("Erro ao consultar CEP: " + enderecoRequest.getCep());
            }
        }
    }

    /**
     * Método de recuperação chamado quando todas as tentativas de retry falharem
     * Este método é chamado automaticamente pelo Spring Retry
     */
    @Recover
    public EnderecoRequestDTO recover(FeignException ex, EnderecoRequestDTO enderecoRequest) {
        logger.error("Todas as tentativas de consulta ao ViaCEP falharam para CEP: {}. " +
                "Status: {}, Mensagem: {}",
                enderecoRequest.getCep(), ex.status(), ex.getMessage());

        throw new RuntimeException("Serviço de CEP temporariamente indisponível. Tente novamente mais tarde.");
    }

    private boolean camposEnderecoCompletos(EnderecoRequestDTO endereco) {
        return endereco.getLogradouro() != null && !endereco.getLogradouro().trim().isEmpty() &&
                endereco.getBairro() != null && !endereco.getBairro().trim().isEmpty() &&
                endereco.getCidade() != null && !endereco.getCidade().trim().isEmpty() &&
                endereco.getUf() != null && !endereco.getUf().trim().isEmpty();
    }

    private EnderecoRequestDTO preencherCamposVazios(EnderecoRequestDTO enderecoRequest, ViaCepResponseDTO viaCepResponse) {
        if (enderecoRequest.getLogradouro() == null || enderecoRequest.getLogradouro().trim().isEmpty()) {
            enderecoRequest.setLogradouro(viaCepResponse.getLogradouro());
        }

        if (enderecoRequest.getBairro() == null || enderecoRequest.getBairro().trim().isEmpty()) {
            enderecoRequest.setBairro(viaCepResponse.getBairro());
        }

        if (enderecoRequest.getCidade() == null || enderecoRequest.getCidade().trim().isEmpty()) {
            enderecoRequest.setCidade(viaCepResponse.getLocalidade());
        }

        if (enderecoRequest.getUf() == null || enderecoRequest.getUf().trim().isEmpty()) {
            enderecoRequest.setUf(viaCepResponse.getUf());
        }

        // Se o complemento veio vazio mas o ViaCEP tem informação, preenche
        if ((enderecoRequest.getComplemento() == null || enderecoRequest.getComplemento().trim().isEmpty())
                && viaCepResponse.getComplemento() != null && !viaCepResponse.getComplemento().trim().isEmpty()) {
            enderecoRequest.setComplemento(viaCepResponse.getComplemento());
        }

        return enderecoRequest;
    }
}

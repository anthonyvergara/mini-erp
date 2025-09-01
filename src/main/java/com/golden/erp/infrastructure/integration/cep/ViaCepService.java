package com.golden.erp.infrastructure.integration.cep;

import com.golden.erp.dto.cep.ViaCepResponseDTO;
import com.golden.erp.dto.cliente.EnderecoRequestDTO;
import com.golden.erp.exception.CepInvalidoException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.annotation.Backoff;
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

    public EnderecoRequestDTO preencherEnderecoComViaCep(EnderecoRequestDTO enderecoRequest) {
        if (enderecoRequest.getCep() == null || enderecoRequest.getCep().trim().isEmpty()) {
            logger.debug("CEP não informado, retornando endereço sem alterações");
            return enderecoRequest;
        }

        if (camposEnderecoCompletos(enderecoRequest)) {
            logger.debug("Endereço já completo, não será consultado o ViaCEP");
            return enderecoRequest;
        }

        ViaCepService selfProxy = context.getBean(ViaCepService.class);
        return selfProxy.consultarViaCep(enderecoRequest);
    }

    @Retryable(
            retryFor = {FeignException.class},
            noRetryFor = {FeignException.BadRequest.class, FeignException.NotFound.class, CepInvalidoException.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public EnderecoRequestDTO consultarViaCep(EnderecoRequestDTO enderecoRequest) {
        logger.info("Consultando ViaCEP para CEP: {}", enderecoRequest.getCep());

        try {
            ViaCepResponseDTO viaCepResponse = viaCepClient.consultarCep(enderecoRequest.getCep());

            if (viaCepResponse.getErro() != null && viaCepResponse.getErro()) {
                logger.warn("ViaCEP retornou erro para CEP: {}", enderecoRequest.getCep());
                throw new CepInvalidoException("CEP inválido: " + enderecoRequest.getCep());
            }

            logger.info("Dados do ViaCEP obtidos com sucesso para CEP: {}", enderecoRequest.getCep());
            return preencherCamposVazios(enderecoRequest, viaCepResponse);

        } catch (FeignException.BadRequest e) {
            logger.error("CEP inválido (400): {}", enderecoRequest.getCep());
            throw new CepInvalidoException("CEP inválido: " + enderecoRequest.getCep());
        } catch (FeignException e) {
            logger.error("Erro ao consultar ViaCEP para CEP {}: {} (Status: {})",
                    enderecoRequest.getCep(), e.getMessage(), e.status());

            if (e.status() >= 500) {
                throw e;
            } else {
                throw new RuntimeException("Erro ao consultar CEP: " + enderecoRequest.getCep());
            }
        }
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

        if ((enderecoRequest.getComplemento() == null || enderecoRequest.getComplemento().trim().isEmpty())
                && viaCepResponse.getComplemento() != null && !viaCepResponse.getComplemento().trim().isEmpty()) {
            enderecoRequest.setComplemento(viaCepResponse.getComplemento());
        }

        return enderecoRequest;
    }
}

# API Mini-ERP - Documentação

## Visão Geral
API REST para gerenciamento de clientes e produtos com integração ao ViaCEP.

## Configuração

### Banco de Dados
1. Instale o PostgreSQL
2. Crie o banco: `CREATE DATABASE "mini-erp";`
3. Configure as credenciais no `application.properties`

### Execução
```bash
mvn spring-boot:run
```

A aplicação rodará em: `http://localhost:8080`

## Endpoints da API

### Clientes

#### POST /clientes
Cria um novo cliente com preenchimento automático do endereço via ViaCEP.

**Request:**
```json
{
  "nome": "João Silva",
  "email": "joao.silva@email.com",
  "cpf": "12345678901",
  "endereco": {
    "numero": "123",
    "complemento": "Apto 101",
    "cep": "01310100"
  }
}
```

**Response (201):**
```json
{
  "id": 1,
  "nome": "João Silva",
  "email": "joao.silva@email.com",
  "cpf": "12345678901",
  "endereco": {
    "logradouro": "Rua das Flores",
    "numero": "123",
    "complemento": "Apto 101",
    "bairro": "Centro",
    "cidade": "São Paulo",
    "uf": "SP",
    "cep": "01310100"
  }
}
```

#### GET /clientes/{id}
Busca cliente por ID.

#### GET /clientes?nome=João&page=0&size=10
Lista clientes com filtro opcional por nome e paginação.

#### PUT /clientes/{id}
Atualiza cliente existente.

#### DELETE /clientes/{id}
Remove cliente.

### Produtos

#### POST /produtos
Cria um novo produto.

**Request:**
```json
{
  "sku": "PROD001",
  "nome": "Notebook Dell Inspiron",
  "precoBruto": 2500.00,
  "estoque": 10,
  "estoqueMinimo": 2,
  "ativo": true
}
```

**Response (201):**
```json
{
  "id": 1,
  "sku": "PROD001",
  "nome": "Notebook Dell Inspiron",
  "precoBruto": 2500.00,
  "estoque": 10,
  "estoqueMinimo": 2,
  "ativo": true
}
```

#### GET /produtos/{id}
Busca produto por ID.

#### GET /produtos?nome=Notebook&sku=PROD&ativo=true&page=0&size=10
Lista produtos com filtros opcionais e paginação.

#### PUT /produtos/{id}
Atualiza produto existente.

#### DELETE /produtos/{id}
Remove produto.

## Validações

### Cliente
- Nome: obrigatório, máx 255 caracteres
- Email: obrigatório, formato válido, único
- CPF: obrigatório, 11 dígitos, único
- CEP: obrigatório, 8 dígitos
- Número: obrigatório, máx 20 caracteres

### Produto
- SKU: obrigatório, máx 50 caracteres, único
- Nome: obrigatório, máx 255 caracteres
- Preço: obrigatório, > 0, máx 10 dígitos + 2 decimais
- Estoque: obrigatório, ≥ 0
- Estoque mínimo: obrigatório, ≥ 0
- Ativo: obrigatório

## Tratamento de Erros

### Códigos de Status
- 200: Sucesso
- 201: Criado
- 204: Sem conteúdo (exclusão)
- 400: Dados inválidos ou CEP inválido
- 404: Recurso não encontrado
- 409: Conflito (email/CPF/SKU já existe)
- 422: Regra de negócio violada
- 500: Erro interno

### Formato de Erro
```json
{
  "message": "Dados inválidos",
  "errors": {
    "email": "Email deve ter um formato válido",
    "cpf": "CPF deve conter exatamente 11 dígitos"
  },
  "timestamp": "2025-08-29T21:30:00",
  "status": 400
}
```

## Integração ViaCEP

Ao criar/atualizar clientes, o sistema:
1. Consulta o ViaCEP com o CEP informado
2. Preenche automaticamente campos vazios (logradouro, bairro, cidade, uf)
3. Trata erros de CEP inválido ou indisponibilidade do serviço

## Exemplos de Teste

### Teste com cURL

```bash
# Criar cliente
curl -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -u admin:admin \
  -d '{
    "nome": "Maria Santos",
    "email": "maria@email.com",
    "cpf": "98765432109",
    "endereco": {
      "numero": "1000",
      "cep": "01310200"
    }
  }'

# Criar produto
curl -X POST http://localhost:8080/produtos \
  -H "Content-Type: application/json" \
  -u admin:admin \
  -d '{
    "sku": "MOUSE001",
    "nome": "Mouse Gamer",
    "precoBruto": 89.90,
    "estoque": 50,
    "estoqueMinimo": 5,
    "ativo": true
  }'

# Listar clientes
curl -X GET "http://localhost:8080/clientes?page=0&size=10" \
  -u admin:admin

# Buscar produto por ID
curl -X GET http://localhost:8080/produtos/1 \
  -u admin:admin
```

## Logs e Monitoramento

A aplicação gera logs detalhados para:
- Operações CRUD
- Integração com ViaCEP
- Erros e exceções
- Performance de consultas SQL

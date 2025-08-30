
👉 Este guia deve ser seguido em todos os prompts e implementações referentes ao projeto Mini-ERP, garantindo padronização, boas práticas e consistência no código.

# Guia de Boas Práticas - Mini-ERP

## 🏗️ Arquitetura em Camadas
O projeto segue uma arquitetura em camadas baseada no padrão Spring Boot + JPA:

### Controller (API Layer)
- Exposição de endpoints REST.
- Responsável por receber requisições HTTP, chamar serviços e retornar respostas.
- Nunca contém regras de negócio.

### Service (Business Layer)
- Contém as regras de negócio da aplicação.
- Faz orquestração entre repositórios e integrações externas.
- Garante consistência das operações.

### Repository (Persistence Layer)
- Comunicação com o banco via Spring Data JPA.
- Apenas consultas e persistência (CRUD).

### Domain / Entities (Model Layer)
- Representação das entidades de negócio (Cliente, Produto, Pedido, etc).
- Contêm anotações JPA e constraints de banco.

### DTOs (Data Transfer Objects)
- Usados para entrada/saída de dados na API.
- Evitam expor diretamente as entidades.
- Aplicar validações com Bean Validation (@NotNull, @Email, @Size, etc).

## 🌐 Boas Práticas REST

### Nomenclatura de Endpoints
- Sempre no plural.
- Exemplo:
    - `/clientes`
    - `/produtos`
    - `/pedidos/{id}/pagar`

### HTTP Status Codes
- `200 OK` → Requisição bem-sucedida.
- `201 Created` → Registro criado.
- `204 No Content` → Exclusão bem-sucedida.
- `400 Bad Request` → Dados inválidos.
- `404 Not Found` → Registro não encontrado.
- `409 Conflict` → Violação de regra (ex: email já existente).
- `422 Unprocessable Entity` → Regras de negócio não atendidas (ex: estoque insuficiente).
- `500 Internal Server Error` → Erro inesperado.

### Paginação e Filtros
- Utilizar query params padrão:
    - `/produtos?page=0&size=10&sort=nome,asc`
    - `/clientes?nome=joao`

### Idempotência e consistência
- Métodos POST criam recursos.
- Métodos PUT atualizam completamente.
- Métodos PATCH atualizam parcialmente.
- Métodos DELETE removem.

## 📦 DTOs e Validação
- DTOs de entrada → Contêm apenas os campos esperados na requisição.
- DTOs de saída → Contêm apenas os dados necessários para retorno.

### Validação com Bean Validation
- Respostas de erro devem seguir um padrão consistente (ex: objeto JSON com mensagem, erros, timestamp).


# Mini-ERP - Sistema de Gestão Simplificado

## 📋 Descrição do Projeto

Sistema Mini-ERP desenvolvido em Spring Boot para gerenciamento de clientes, produtos e pedidos com integração de APIs externas, autenticação JWT e controle de estoque automatizado.

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Docker e Docker Compose instalados
- Git instalado

### Clonando e Executando

```bash
# 1. Clone o repositório
git clone <url-do-repositorio>
cd mini-erp

# 2. Execute com Docker Compose
docker-compose up --build

# 3. Acesse a aplicação
# API: http://localhost:8080
# Banco PostgreSQL: localhost:5433
```

A aplicação será executada automaticamente com:
- ✅ Banco PostgreSQL configurado
- ✅ Migrações Liquibase aplicadas
- ✅ Dados iniciais carregados
- ✅ API REST disponível na porta 8080

### Usuário Inicial
```
Admin: username=admin, password=admin123
```

## 🏗️ Arquitetura do Projeto

### Estrutura de Pastas

```
src/main/java/com/golden/erp/
├── controller/         # Endpoints REST (API Layer)
├── service/            # Regras de negócio (Business Layer)
├── domain/             # Entidades JPA (Model Layer)
├── dto/                # Data Transfer Objects
├── interfaces/         # Contratos de serviços
├── mapper/             # Conversão entre entidades e DTOs
├── exception/          # Tratamento de exceções
└── infrastructure/     # Configurações e integrações
    ├── config/         # Configurações
    ├── integration/    # APIs externas (ViaCEP, Exchange)
    ├── repository/     # Acesso a dados (Persistence Layer)
    ├── scheduler/      # Tarefas agendadas
    └── security/       # Configurações de segurança JWT

src/main/resources/
├── db/changelog/       # Scripts Liquibase
├── application.properties
└── logback-spring.xml  # Configuração de logs
```

### Princípios Arquiteturais

Optei por utilizar uma arquitetura em camadas para garantir clareza, manutenibilidade e escalabilidade do projeto, sem adicionar complexidade desnecessária. 
Essa abordagem é bastante consolidada no mercado e acredito que facilita a evolução natural do sistema.<br>

Além de separar as responsabilidades, essa estrutura segue boas práticas de desenvolvimento, promovendo reutilização de código e baixo acoplamento. Assim, novos recursos podem ser adicionados ou alterados sem impactar diretamente outras camadas, mantendo o projeto flexível e sustentável a longo prazo.

Essa estrutura está separada em camadas lógicas, cada uma com sua responsabilidade definida:
- **Controller**: Expõe os endpoints REST e facilita a comunicação com o cliente.
- **Service**: Centraliza as regras de negócio.
- **Domain**: Define as entidades do sistema.
- **DTOs e Mapper**: Evitam acoplamento entre camadas e garantem transferência de dados limpa.
- **Exception**: Centraliza o tratamento de erros da aplicação, padronizando mensagens e status HTTP.
- **Interfaces**: Definem contratos claros para os serviços, promovendo baixo acoplamento.
- **Infrastructure**: Concentra persistência, integrações externas, segurança e configurações, mantendo o core da aplicação independente.

## 🔧 Tecnologias e Recursos

### Stack Principal
- **Spring Boot 3.3.1** - Framework principal
- **Java 17** - Linguagem de programação
- **PostgreSQL** - Banco de dados
- **Liquibase** - Controle de versão do banco
- **Docker** - Containerização

### Recursos Implementados

#### 🔐 **Autenticação e Autorização**
- JWT (JSON Web Token)
- Roles: USER e ADMIN
- Endpoints protegidos por role
- Refresh tokens

#### 🌐 **Integrações Externas**
- **ViaCEP**: Preenchimento automático de endereços
- **Exchange Rate API**: Cotação BRL → USD
- **Feign Client**: Cliente HTTP declarativo
- **Retry**: Política de retry automática

#### 📊 **Funcionalidades de Negócio**
- Gestão de clientes, produtos e pedidos
- Controle de estoque
- Cálculos financeiros
- Soft delete para auditoria
- Status de pedidos (CREATED, PAID, CANCELLED, LATE)

#### ⚡ **Performance e Monitoramento**
- Cache em memória para cotações USD
- Logs estruturados em JSON
- Schedulers para tarefas automatizadas
- Paginação em listagens

#### 🛡️ **Qualidade e Confiabilidade**
- Validações Bean Validation
- Tratamento global de exceções
- Transações ACID
- Campos de auditoria automáticos

## 📡 Endpoints da API

### 🔐 Autenticação (`/api/auth`)
```
POST   /api/auth/register     - Registrar novo usuário
POST   /api/auth/login        - Login
```

### 👥 Usuários (`/api/usuarios`) - Requer autenticação
```
GET    /api/usuarios/me       - Dados do usuário logado
GET    /api/usuarios          - Listar usuários (ADMIN)
GET    /api/usuarios/{id}     - Buscar usuário por ID
PUT    /api/usuarios/{id}/status - Ativar/desativar usuário (ADMIN)
```

### ✅ Métricas (`/actuator/metrics`)
```
GET     /actuator/metrics/pedidos.criados                           - Quantidade total de pedidos criados
GET     /actuator/metrics/pedidos.criados.por_hora?tag=hora:12      - Quantidade de pedidos criados por hora especifica
GET     /actuator/metrics/pedidos.criados.por_hora                  - Quantidade de pedidos criados por hora
```

### 👤 Clientes (`/clientes`) - Requer autenticação
```
POST   /clientes              - Criar cliente (com integração ViaCEP)
GET    /clientes/{id}         - Buscar cliente por ID
GET    /clientes              - Listar clientes (filtro: nome, paginação)
PUT    /clientes/{id}         - Atualizar cliente
DELETE /clientes/{id}         - Excluir cliente
```

### 📦 Produtos (`/produtos`) - Requer autenticação
```
POST   /produtos              - Criar produto
GET    /produtos/{id}         - Buscar produto por ID
GET    /produtos              - Listar produtos (filtros: nome, sku, ativo, paginação)
PUT    /produtos/{id}         - Atualizar produto
DELETE /produtos/{id}         - Excluir produto
```

### 🛒 Pedidos (`/pedidos`) - Requer autenticação
```
POST   /pedidos                 - Criar pedido (baixa estoque automaticamente)
GET    /pedidos/{id}            - Buscar pedido por ID
GET    /pedidos                 - Listar pedidos (filtros: clienteId, status, paginação)
PATCH  /pedidos/{id}/pagar      - Marcar pedido como pago
PATCH  /pedidos/{id}/cancelar   - Cancelar pedido (devolve estoque)
DELETE /pedidos/{id}            - Soft delete do pedido
GET    /pedidos/{id}/usd-total  - Obter total do pedido em USD
```

### Exemplos de Uso com `curl`
### Autenticação
```bash
# Fazer login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### Criar Cliente
```bash
curl -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {seu-jwt-token}" \
  -d '{
    "nome": "Anthony Vergara",
    "email": "anthony.vergara@email.com",
    "cpf": "12345678901",
    "endereco": {
      "numero": "123",
      "cep": "01310100"
    }
  }'
```

### Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

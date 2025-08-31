--liquibase formatted sql

--changeset anthony:006-create-indexes
-- Índices para tabela usuarios
CREATE INDEX idx_usuarios_username ON usuarios(username);
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_ativo ON usuarios(ativo);
CREATE INDEX idx_usuarios_deleted_at ON usuarios(deleted_at);

-- Índices para tabela clientes
CREATE INDEX idx_clientes_email ON clientes(email);
CREATE INDEX idx_clientes_cpf ON clientes(cpf);
CREATE INDEX idx_clientes_nome ON clientes(nome);
CREATE INDEX idx_clientes_deleted_at ON clientes(deleted_at);

-- Índices para tabela produto
CREATE INDEX idx_produto_sku ON produto(sku);
CREATE INDEX idx_produto_nome ON produto(nome);
CREATE INDEX idx_produto_ativo ON produto(ativo);
CREATE INDEX idx_produto_deleted_at ON produto(deleted_at);

-- Índices para tabela pedidos
CREATE INDEX idx_pedidos_cliente_id ON pedidos(cliente_id);
CREATE INDEX idx_pedidos_status ON pedidos(status);
CREATE INDEX idx_pedidos_created_at ON pedidos(created_at);
CREATE INDEX idx_pedidos_deleted_at ON pedidos(deleted_at);

-- Índices para tabela item_pedido
CREATE INDEX idx_item_pedido_pedido_id ON item_pedido(pedido_id);
CREATE INDEX idx_item_pedido_produto_id ON item_pedido(produto_id);

--rollback DROP INDEX IF EXISTS idx_usuarios_username;
--rollback DROP INDEX IF EXISTS idx_usuarios_email;
--rollback DROP INDEX IF EXISTS idx_usuarios_ativo;
--rollback DROP INDEX IF EXISTS idx_usuarios_deleted_at;
--rollback DROP INDEX IF EXISTS idx_clientes_email;
--rollback DROP INDEX IF EXISTS idx_clientes_cpf;
--rollback DROP INDEX IF EXISTS idx_clientes_nome;
--rollback DROP INDEX IF EXISTS idx_clientes_deleted_at;
--rollback DROP INDEX IF EXISTS idx_produto_sku;
--rollback DROP INDEX IF EXISTS idx_produto_nome;
--rollback DROP INDEX IF EXISTS idx_produto_ativo;
--rollback DROP INDEX IF EXISTS idx_produto_deleted_at;
--rollback DROP INDEX IF EXISTS idx_pedidos_cliente_id;
--rollback DROP INDEX IF EXISTS idx_pedidos_status;
--rollback DROP INDEX IF EXISTS idx_pedidos_created_at;
--rollback DROP INDEX IF EXISTS idx_pedidos_deleted_at;
--rollback DROP INDEX IF EXISTS idx_item_pedido_pedido_id;
--rollback DROP INDEX IF EXISTS idx_item_pedido_produto_id;

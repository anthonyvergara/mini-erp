--liquibase formatted sql

--changeset anthony:007-insert-initial-data
-- Inserir usuário administrador padrão
INSERT INTO usuarios (username, email, password, nome_completo, role, ativo)
VALUES ('admin', 'admin@golden.com', '$2a$12$uD8IFDK577jP4L9h65MXCOPW3kY302hXNIBufmoHmKsuFK2hjG4lW', 'Administrador Sistema', 'ADMIN', true);

-- Inserir alguns produtos de exemplo
INSERT INTO produto (sku, nome, preco_bruto, estoque, estoque_minimo, ativo)
VALUES
    ('PROD001', 'Notebook Dell Inspiron 15', 2899.99, 10, 2, true),
    ('PROD002', 'Mouse Logitech MX Master 3', 459.99, 25, 5, true),
    ('PROD003', 'Teclado Mecânico Corsair K95', 899.99, 15, 3, true),
    ('PROD004', 'Monitor Samsung 27" 4K', 1599.99, 8, 2, true),
    ('PROD005', 'Webcam Logitech C920', 299.99, 20, 5, true);

-- Inserir cliente de exemplo
INSERT INTO clientes (nome, email, cpf, logradouro, numero, complemento, bairro, cidade, uf, cep)
VALUES ('João Silva Santos', 'joao.silva@email.com', '12345678901', 'Rua das Flores', '123', 'Apto 45', 'Centro', 'São Paulo', 'SP', '01234567');

--rollback DELETE FROM usuarios WHERE username = 'admin';
--rollback DELETE FROM produto WHERE sku IN ('PROD001', 'PROD002', 'PROD003', 'PROD004', 'PROD005');
--rollback DELETE FROM clientes WHERE email = 'joao.silva@email.com';

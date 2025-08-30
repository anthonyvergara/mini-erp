-- Script para criar o banco de dados mini-erp no PostgreSQL

-- Conecte-se ao PostgreSQL como superusuário e execute:
-- CREATE DATABASE "mini-erp";

-- As tabelas serão criadas automaticamente pelo Hibernate quando a aplicação for executada
-- devido à configuração spring.jpa.hibernate.ddl-auto=update

-- Exemplos de dados para teste (opcional - execute após a aplicação estar rodando)

-- Inserir produtos de exemplo
INSERT INTO produtos (sku, nome, preco_bruto, estoque, estoque_minimo, ativo) VALUES
('PROD001', 'Notebook Dell Inspiron', 2500.00, 10, 2, true),
('PROD002', 'Mouse Logitech MX Master', 250.00, 50, 5, true),
('PROD003', 'Teclado Mecânico RGB', 450.00, 25, 3, true),
('PROD004', 'Monitor LG 24"', 800.00, 15, 2, true),
('PROD005', 'Cabo HDMI 2m', 35.00, 100, 10, true);

-- Inserir clientes de exemplo
INSERT INTO clientes (nome, email, cpf, logradouro, numero, complemento, bairro, cidade, uf, cep) VALUES
('João Silva', 'joao.silva@email.com', '12345678901', 'Rua das Flores', '123', 'Apto 101', 'Centro', 'São Paulo', 'SP', '01310100'),
('Maria Santos', 'maria.santos@email.com', '98765432109', 'Avenida Paulista', '1000', 'Sala 50', 'Bela Vista', 'São Paulo', 'SP', '01310200'),
('Pedro Oliveira', 'pedro.oliveira@email.com', '45678912345', 'Rua Augusta', '500', '', 'Consolação', 'São Paulo', 'SP', '01305000');

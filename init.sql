-- Inicialização do banco de dados Pets API
-- Este script é executado automaticamente quando o container MySQL é criado

-- Configuração de charset e collation
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection=utf8mb4;

-- Criação do banco de dados (se não existir)
CREATE DATABASE IF NOT EXISTS pets
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Seleciona o banco de dados
USE pets;

-- Criação do usuário específico para a aplicação (opcional)
-- CREATE USER IF NOT EXISTS 'pets_user'@'%' IDENTIFIED BY 'pets_pass';
-- GRANT ALL PRIVILEGES ON pets.* TO 'pets_user'@'%';
-- FLUSH PRIVILEGES;

-- Comentário sobre as tabelas que serão criadas automaticamente pelo Hibernate
-- As tabelas serão criadas automaticamente com base nas entidades JPA:
-- - users
-- - verification_tokens  
-- - pets
-- - services

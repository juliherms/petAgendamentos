-- Script de inicialização para o módulo de agendamento
-- Execute este script após a criação das tabelas principais (users, pets, services)

-- Tabela de agendamentos
CREATE TABLE IF NOT EXISTS agendamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    servico_id BIGINT NOT NULL,
    prestador_id BIGINT NOT NULL,
    data DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Índices para performance
    INDEX idx_agendamento_prestador_data_hora (prestador_id, data, hora_inicio),
    INDEX idx_agendamento_pet (pet_id),
    INDEX idx_agendamento_servico (servico_id),
    INDEX idx_agendamento_data (data),
    INDEX idx_agendamento_status (status),
    
    -- Constraints de integridade referencial
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
    FOREIGN KEY (servico_id) REFERENCES services(id) ON DELETE CASCADE,
    FOREIGN KEY (prestador_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Constraint único para evitar conflitos de horário
    UNIQUE KEY uk_prestador_data_hora (prestador_id, data, hora_inicio)
);

-- Tabela de configuração de horário
CREATE TABLE IF NOT EXISTS configuracoes_horario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dia_semana VARCHAR(20) NOT NULL UNIQUE,
    hora_abertura TIME NOT NULL,
    hora_fechamento TIME NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Índices
    INDEX idx_config_horario_dia (dia_semana),
    INDEX idx_config_horario_ativo (ativo)
);

-- Inserir configurações padrão de horário comercial
INSERT INTO configuracoes_horario (dia_semana, hora_abertura, hora_fechamento, ativo) VALUES
('MONDAY', '09:00:00', '18:00:00', TRUE),
('TUESDAY', '09:00:00', '18:00:00', TRUE),
('WEDNESDAY', '09:00:00', '18:00:00', TRUE),
('THURSDAY', '09:00:00', '18:00:00', TRUE),
('FRIDAY', '09:00:00', '18:00:00', TRUE),
('SATURDAY', '09:00:00', '18:00:00', TRUE)
ON DUPLICATE KEY UPDATE
    hora_abertura = VALUES(hora_abertura),
    hora_fechamento = VALUES(hora_fechamento),
    ativo = VALUES(ativo),
    updated_at = CURRENT_TIMESTAMP;

-- Comentários das tabelas
ALTER TABLE agendamentos COMMENT = 'Tabela de agendamentos de serviços para pets';
ALTER TABLE configuracoes_horario COMMENT = 'Configurações de horário comercial por dia da semana';

-- Comentários das colunas principais
ALTER TABLE agendamentos 
    MODIFY COLUMN status VARCHAR(20) COMMENT 'Status: AGENDADO, CONFIRMADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO',
    MODIFY COLUMN hora_inicio TIME COMMENT 'Horário de início (deve ser hora cheia)',
    MODIFY COLUMN hora_fim TIME COMMENT 'Horário de fim (calculado automaticamente: hora_inicio + 1h)';

-- Verificar se as tabelas foram criadas corretamente
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME IN ('agendamentos', 'configuracoes_horario');

-- Verificar configurações de horário
SELECT 
    dia_semana,
    hora_abertura,
    hora_fechamento,
    ativo
FROM configuracoes_horario
ORDER BY 
    CASE dia_semana
        WHEN 'MONDAY' THEN 1
        WHEN 'TUESDAY' THEN 2
        WHEN 'WEDNESDAY' THEN 3
        WHEN 'THURSDAY' THEN 4
        WHEN 'FRIDAY' THEN 5
        WHEN 'SATURDAY' THEN 6
        WHEN 'SUNDAY' THEN 7
    END;


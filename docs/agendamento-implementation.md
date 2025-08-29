# Módulo de Agendamento - Pets API

## Visão Geral

O módulo de agendamento implementa a funcionalidade de agendamento de serviços para pets, seguindo todas as regras de negócio especificadas na história de usuário. Cada agendamento ocupa um slot de 1 hora a partir do horário de início.

## Arquitetura

### Estrutura do Módulo

```
agendamento/
├── api/                           # Interface pública do módulo
│   ├── package-info.java         # @NamedInterface("api")
│   └── AgendamentoApi.java       # DTOs e eventos
├── internal/                      # Implementação interna
│   ├── domain/                   # Entidades de domínio
│   │   ├── Agendamento.java      # Entidade principal
│   │   └── ConfiguracaoHorario.java # Configuração de horário
│   ├── repo/                     # Repositórios
│   │   ├── AgendamentoRepository.java
│   │   └── ConfiguracaoHorarioRepository.java
│   ├── service/                  # Serviços de negócio
│   │   ├── AgendamentoService.java
│   │   └── ConfiguracaoHorarioService.java
│   ├── web/                      # Controladores REST
│   │   └── AgendamentoController.java
│   └── exception/                # Exceções específicas
│       └── AgendamentoExceptionHandler.java
└── package-info.java             # @ApplicationModule
```

### Dependências Permitidas

O módulo pode depender de:
- `users::api` - Para validação de usuários e prestadores
- `pets::api` - Para validação de pets
- `services::api` - Para validação de serviços

## Regras de Negócio Implementadas

### 1. Validação de Horário Comercial
- **Janela de atendimento**: Segunda a Sábado, 09:00-18:00
- **Configurável**: Via tabela `configuracoes_horario`
- **Validação**: Horário de início deve estar dentro da janela

### 2. Duração Fixa do Serviço
- **Slot fixo**: Cada agendamento ocupa exatamente 1 hora
- **Cálculo automático**: `horaFim = horaInicio + 1h`

### 3. Exclusividade de Horário
- **Sem conflitos**: Não pode haver dois pets agendados no mesmo horário para o mesmo prestador
- **Constraint único**: `(prestadorId, data, horaInicio)`
- **Validação transacional**: Verificação antes da criação

### 4. Alinhamento à Hora Cheia
- **Formato obrigatório**: HH:00 (ex: 09:00, 10:00, 14:00)
- **Validação**: Rejeita horários como 09:30, 10:15, etc.

### 5. Validação de Data/Hora
- **Não no passado**: Data e hora devem ser futuras
- **Dia útil**: Domingo não é permitido
- **Fuso horário**: America/Recife (configurável)

### 6. Integridade de Dados
- **Pet válido**: Deve existir e pertencer a usuário ativo
- **Serviço ativo**: Deve existir e estar ativo
- **Prestador válido**: Deve existir, estar ativo e ter perfil PROVEDOR

## Endpoints Disponíveis

### POST `/agendamentos`
Cria um novo agendamento.

**Request:**
```json
{
  "petId": 1,
  "servicoId": 1,
  "prestadorId": 2,
  "data": "2025-01-15",
  "horaInicio": "10:00"
}
```

**Response (201):**
```json
{
  "id": 1,
  "petId": 1,
  "servicoId": 1,
  "prestadorId": 2,
  "data": "2025-01-15",
  "horaInicio": "10:00",
  "horaFim": "11:00",
  "status": "AGENDADO",
  "createdAt": "2025-01-14T10:00:00-03:00"
}
```

### GET `/agendamentos/usuario/{usuarioId}`
Lista agendamentos de um usuário específico.

### GET `/agendamentos/pet/{petId}`
Lista agendamentos de um pet específico.

## Validações e Tratamento de Erros

### Códigos de Status HTTP

- **201 Created**: Agendamento criado com sucesso
- **400 Bad Request**: Dados inválidos no request
- **409 Conflict**: Horário indisponível (conflito)
- **422 Unprocessable Entity**: Validações de negócio falharam
- **500 Internal Server Error**: Erro interno do servidor

### Mensagens de Erro

- **Horário fora do comercial**: "Horário fora do horário comercial (09:00 - 18:00)"
- **Dia indisponível**: "Domingo não é dia de funcionamento"
- **Horário não alinhado**: "Horário inválido; use horas cheias (ex: 09:00, 10:00)"
- **Data/hora no passado**: "Data/hora no passado"
- **Horário indisponível**: "Horário indisponível para o prestador"
- **Pet não encontrado**: "Pet não encontrado com ID: X"
- **Serviço não encontrado**: "Serviço não encontrado com ID: X"
- **Prestador não encontrado**: "Prestador não encontrado com ID: X"

## Configuração de Horário Comercial

### Tabela `configuracoes_horario`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | BIGINT | Chave primária |
| `dia_semana` | VARCHAR(20) | Dia da semana (MONDAY, TUESDAY, etc.) |
| `hora_abertura` | TIME | Horário de abertura (ex: 09:00) |
| `hora_fechamento` | TIME | Horário de fechamento (ex: 18:00) |
| `ativo` | BOOLEAN | Se a configuração está ativa |
| `created_at` | TIMESTAMP | Data de criação |
| `updated_at` | TIMESTAMP | Data de última atualização |

### Configuração Padrão

- **Segunda a Sábado**: 09:00 - 18:00
- **Domingo**: Não configurado (não funciona)
- **Configurável**: Pode ser alterada via banco de dados

## Eventos de Domínio

### AgendamentoCriadoEvent

Publicado quando um agendamento é criado com sucesso.

```java
record AgendamentoCriadoEvent(
    Long agendamentoId,
    Long petId,
    Long servicoId,
    Long prestadorId,
    LocalDate data,
    LocalTime horaInicio,
    LocalTime horaFim
) {}
```

**Uso**: Outros módulos podem escutar este evento para:
- Enviar confirmações por e-mail/SMS
- Atualizar calendários
- Gerar relatórios
- Notificações push

## Performance e Escalabilidade

### Índices de Banco

- **`idx_agendamento_prestador_data_hora`**: Para consultas de disponibilidade
- **`idx_agendamento_pet`**: Para consultas por pet
- **`idx_agendamento_servico`**: Para consultas por serviço
- **`idx_agendamento_data`**: Para consultas por data
- **`idx_agendamento_status`**: Para consultas por status

### Constraints

- **Foreign Keys**: Integridade referencial com `users`, `pets`, `services`
- **Unique Key**: `(prestadorId, data, horaInicio)` para evitar conflitos
- **Cascade Delete**: Agendamentos são removidos quando pet/serviço/prestador é removido

## Testes

### Testes de Módulo

- **`AgendamentoModuleTests`**: Verifica estrutura modular
- **`AgendamentoModuleIntegrationTest`**: Testes de integração

### Cenários de Teste

1. **Agendamento com sucesso**: Horário válido, dados corretos
2. **Horário fora do comercial**: Antes de 09:00 ou após 18:00
3. **Horário não alinhado**: Minutos diferentes de 00
4. **Domingo**: Dia não funcionante
5. **Conflito de horário**: Mesmo prestador, data e hora
6. **Dados inválidos**: Pet/serviço/prestador inexistentes

## Monitoramento e Logs

### Logs de Auditoria

```
[AGENDAMENTO] Iniciando criação de agendamento para pet 1, serviço 1, prestador 2
[AGENDAMENTO] Agendamento criado com sucesso: ID 1
[CONFIGURACAO] Configurações de horário comercial inicializadas com sucesso
```

### Métricas Sugeridas

- Agendamentos criados por dia
- Taxa de sucesso vs. falha
- Horários mais populares
- Tempo médio de processamento
- Conflitos de horário

## Configuração por Ambiente

### Desenvolvimento
- Configurações padrão de horário
- Logs detalhados
- Validações rigorosas

### Produção
- Configurações personalizadas de horário
- Logs de nível INFO
- Validações otimizadas
- Monitoramento de performance

## Roadmap e Melhorias Futuras

### Funcionalidades Planejadas

1. **Cancelamento/Reagendamento**: Endpoints para modificar agendamentos
2. **Notificações**: Lembretes automáticos por e-mail/SMS
3. **Calendário**: Visualização de disponibilidade
4. **Recorrência**: Agendamentos recorrentes
5. **Timeout**: Liberação automática de slots não confirmados

### Melhorias Técnicas

1. **Cache**: Cache de configurações de horário
2. **Async**: Processamento assíncrono de eventos
3. **Retry**: Mecanismo de retry para falhas
4. **Circuit Breaker**: Proteção contra falhas externas
5. **Métricas**: Métricas detalhadas de performance

## Troubleshooting

### Problemas Comuns

#### Falha na Criação de Agendamento
- Verificar se pet/serviço/prestador existem
- Validar horário comercial
- Verificar conflitos de horário
- Confirmar formato de hora (HH:00)

#### Configuração de Horário
- Verificar tabela `configuracoes_horario`
- Confirmar se dia está ativo
- Validar formato de hora (HH:MM:SS)

#### Performance
- Verificar índices de banco
- Monitorar queries lentas
- Validar constraints únicos

### Debug

- Habilitar logs DEBUG para `com.juliherms.agendamento.pets.agendamento`
- Verificar configurações de horário no banco
- Validar dados de entrada
- Testar endpoints individualmente

# Pets API

API modular (Spring Boot + Spring Modulith) para cadastro de usuários, pets e serviços, com verificação de contato e documentação via Swagger/OpenAPI.

## Visão Geral
- Módulos (via Spring Modulith):
  - `users`: criação e verificação de usuários (gera token e publica evento de notificação).
  - `pets`: cadastro de pets (perfil CLIENTE).
  - `services`: cadastro de serviços (perfil PROVEDOR) com preços por porte.
  - `verificador`: processa eventos de criação e envia verificação via e-mail ou SMS (simulado).
  - `notifications`: módulo para futuras implementações de notificações.
  - **`agendamento`: agendamento de serviços para pets com validação de horário comercial e disponibilidade.**
- Segurança de dados: senha com hash BCrypt; token de verificação armazenado como hash (uso único, TTL 24h).
- Banco: MySQL 8.0 com configurações específicas por ambiente (dev/prod).
- **E-mail**: Sistema de envio com implementações simulada e real (Mailtrap), templates Thymeleaf, configuração por ambiente.

## Pré-requisitos
- Java 21
- Maven 3.6+
- MySQL 8.0+ ou Docker

## Como Executar

### 1. Banco de Dados MySQL

**Opção A: Docker (Recomendado para desenvolvimento)**
```bash
# Inicia o MySQL
docker-compose up -d

# Verifica se está rodando
docker-compose ps
```

**Opção B: MySQL Local**
- Instale MySQL 8.0+
- Crie o banco `pets`
- Configure usuário e senha em `src/main/resources/application-dev.properties`

### 2. Aplicação
```bash
./mvnw spring-boot:run
```
Windows:
```bash
mvnw.cmd spring-boot:run
```

Aplicação padrão: `http://localhost:8080`

### 3. Configuração de E-mail (Opcional)

**Para usar envio real de e-mails via Mailtrap:**

1. Crie uma conta em [mailtrap.io](https://mailtrap.io)
2. Configure as variáveis de ambiente:
```bash
export MAILTRAP_HOST=sandbox.smtp.mailtrap.io
export MAILTRAP_PORT=2525
export MAILTRAP_USER=seu_usuario
export MAILTRAP_PASS=sua_senha
export MAILTRAP_FROM=no-reply@pets.dev
export APP_EMAIL_SERVICE_ACTIVE=emailServiceMailTrap
```

3. Reinicie a aplicação

**Para manter simulação (padrão):**
```bash
export APP_EMAIL_SERVICE_ACTIVE=emailServiceSimulado
```

Veja [docs/mailtrap-implementation.md](docs/mailtrap-implementation.md) para detalhes completos.

## Documentação (Swagger / OpenAPI)
- UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

O bean de configuração se encontra em `com.juliherms.agendamento.pets.config.OpenApiConfig`.

## Endpoints Principais
### Usuários
- POST `/users` — cria usuário e dispara envio de token por evento.
  - Request (exemplo):
    ```json
    {
      "nome": "Maria Silva",
      "email": "maria@example.com",
      "telefone": "+5581999990000",
      "endereco": "Av. Boa Viagem, 1000, Recife/PE",
      "senha": "S3nh@F0rte!",
      "perfil": "CLIENTE",
      "preferenciaVerificacao": "EMAIL"
    }
    ```
  - Response 201: dados do usuário (sem senha) e `status = pendente_verificacao`.

- POST `/users/{id}/verificar` — verifica contato com token e ativa a conta.
  - Request (exemplo):
    ```json
    { "token": "<token do log>", "canal": "EMAIL" }
    ```

Observação: após criar o usuário, o módulo `verificador` processa automaticamente o evento e envia a verificação via e-mail ou SMS conforme preferência. Procure no log as linhas `[VERIFICADOR]`, `[EMAIL]` ou `[SMS]` para acompanhar o processo.

### Pets (CLIENTE)
- POST `/users/{idUsuario}/pets` — cadastra pet associado ao usuário CLIENTE ativo.
  - Request (exemplo):
    ```json
    { "nome": "Rex", "idade": 3, "raca": "Vira-lata", "peso": 18.2 }
    ```

### Serviços (PROVEDOR)
- POST `/users/{idUsuario}/servicos` — cadastra serviço com preços por porte, somente para PROVEDOR ativo.
  - Request (exemplo):
    ```json
    {
      "titulo": "Tosa Completa",
      "descricao": "Tosa higiênica e acabamento",
      "precosPorPorte": { "p": 50.0, "m": 70.0, "g": 90.0 },
      "ativo": true
    }
    ```

### Agendamentos
- POST `/agendamentos` — cria agendamento de serviço para pet (slot de 1h).
  - Request (exemplo):
    ```json
    {
      "petId": 1,
      "servicoId": 1,
      "prestadorId": 2,
      "data": "2025-01-15",
      "horaInicio": "10:00"
    }
    ```
  - Validações: horário comercial (09:00-18:00), dias úteis (seg-sáb), hora cheia, disponibilidade.
- GET `/agendamentos/usuario/{usuarioId}` — lista agendamentos do usuário.
- GET `/agendamentos/pet/{petId}` — lista agendamentos de um pet específico.

### E-mails (Desenvolvimento)
- GET `/emails/info` — informações sobre o serviço de e-mail ativo
- POST `/emails/test?to=email@exemplo.com` — envia e-mail de teste simples
- POST `/emails/test/html?to=email@exemplo.com` — envia e-mail HTML de teste

**Nota:** Endpoints de e-mail disponíveis apenas em desenvolvimento para testes.

## Regras de Negócio (resumo)
- Perfis: `ADMIN`, `CLIENTE`, `PROVEDOR`.
- Estados do usuário: `pendente_verificacao`, `ativo`, `inativo`.
- Unicidade: email único (case-insensitive).
- Telefone no padrão E.164.
- Serviços exigem valores > 0; serviço inativo não pode ser agendado (fora do escopo aqui).

## Erros Padrão
- 400 payload inválido
- 401 não autenticado
- 403 ação não permitida para o perfil/estado
- 409 email já cadastrado
- 422 validação de campos (ex.: telefone fora do padrão, preços ≤ 0)
- 429 limite de envios de verificação (a ser implementado)
- 500 erro interno

## Arquitetura (Spring Modulith)
- `@Modulithic` aplicado na classe `PetsApplication`.
- Cada módulo possui `package-info.java` com `@ApplicationModule` e, quando aplicável, uma interface de API exposta.
- Evento de domínio: `users.api.UserCreatedEvent` publicado ao criar o usuário; o módulo `verificador` consome e envia verificação via e-mail/SMS conforme preferência do usuário.
- Teste de verificação estrutural: `ModularityVerificationTests` executa `ApplicationModules.verify()`.

## Configuração

### Perfis de Ambiente
- **dev**: `src/main/resources/application-dev.properties` - Desenvolvimento local
- **prod**: `src/main/resources/application-prod.properties` - Produção

### Banco de Dados
- **Desenvolvimento**: MySQL local ou Docker com `create-drop` (tabelas recriadas a cada restart)
- **Produção**: MySQL com `validate` (validação de schema)

### Variáveis de Ambiente
Copie `env.example` para `.env` e configure:
- `DB_HOST`, `DB_PORT`, `DB_NAME`
- `DB_USERNAME`, `DB_PASSWORD`
- `SPRING_PROFILES_ACTIVE`

## Desenvolvimento
- Build: `./mvnw clean verify`
- Testes: `./mvnw test`

## Comandos Docker Úteis
```bash
# Iniciar MySQL
docker-compose up -d

# Parar MySQL
docker-compose down

# Ver logs do MySQL
docker-compose logs mysql

# Acessar MySQL CLI
docker exec -it pets-mysql mysql -u root -p
```

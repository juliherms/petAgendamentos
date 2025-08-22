# Pets API

API modular (Spring Boot + Spring Modulith) para cadastro de usuários, pets e serviços, com verificação de contato e documentação via Swagger/OpenAPI.

## Visão Geral
- Módulos (via Spring Modulith):
  - `users`: criação e verificação de usuários (gera token e publica evento de notificação).
  - `pets`: cadastro de pets (perfil CLIENTE).
  - `services`: cadastro de serviços (perfil PROVEDOR) com preços por porte.
  - `verificador`: processa eventos de criação e envia verificação via e-mail ou SMS (simulado).
  - `notifications`: módulo para futuras implementações de notificações.
- Segurança de dados: senha com hash BCrypt; token de verificação armazenado como hash (uso único, TTL 24h).
- Banco: H2 em memória para desenvolvimento.

## Como Executar
```bash
./mvnw spring-boot:run
```
Windows:
```bash
mvnw.cmd spring-boot:run
```

Aplicação padrão: `http://localhost:8080`

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
- H2 em memória: console habilitado (`/h2-console`).
- Propriedades em `src/main/resources/application.properties`.

## Desenvolvimento
- Build: `./mvnw clean verify`
- Testes: `./mvnw test`

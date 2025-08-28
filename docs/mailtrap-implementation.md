# Implementação Mailtrap - Pets API

## Visão Geral

Este projeto implementa um sistema de envio de e-mails com duas implementações:

1. **EmailServiceImpl** (`@Qualifier("emailServiceSimulado")`): Simula o envio de e-mails (desenvolvimento)
2. **EmailServiceMailTrapImpl** (`@Qualifier("emailServiceMailTrap")`): Envia e-mails reais via Mailtrap

## Arquitetura

### Interface EmailService

```java
public interface EmailService {
    boolean enviarEmailVerificacao(String destinatario, String token, Instant expiresAt);
    void enviarEmailTexto(String from, String to, String subject, String body);
    void enviarEmailHtml(String from, String to, String subject, String html, Map<String, byte[]> anexos);
}
```

### Implementações

- **EmailServiceImpl**: Simula envio com logging detalhado
- **EmailServiceMailTrapImpl**: Usa JavaMailSender para envio real via SMTP

## Configuração por Ambiente

### Desenvolvimento (`application-dev.properties`)

```properties
# Email Configuration (Mailtrap Sandbox)
spring.mail.host=${MAILTRAP_HOST:sandbox.smtp.mailtrap.io}
spring.mail.port=${MAILTRAP_PORT:2525}
spring.mail.username=${MAILTRAP_USER}
spring.mail.password=${MAILTRAP_PASS}
spring.mail.protocol=smtp
spring.mail.default-encoding=UTF-8
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# App Configuration
app.mail.from=${MAILTRAP_FROM:no-reply@pets.dev}
app.email.service.active=emailServiceSimulado
```

### Produção (`application-prod.properties`)

```properties
# Email Configuration (Production - Mailtrap Sending ou outro provedor)
spring.mail.host=${SMTP_HOST}
spring.mail.port=${SMTP_PORT:587}
spring.mail.username=${SMTP_USER}
spring.mail.password=${SMTP_PASS}
spring.mail.protocol=smtp
spring.mail.default-encoding=UTF-8
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.connectiontimeout=10000
spring.mail.properties.mail.smtp.timeout=10000
spring.mail.properties.mail.smtp.writetimeout=10000

# App Configuration
app.mail.from=${SMTP_FROM:no-reply@pets.com}
app.email.service.active=emailServiceMailTrap
```

## Variáveis de Ambiente

### Desenvolvimento
```bash
MAILTRAP_HOST=sandbox.smtp.mailtrap.io
MAILTRAP_PORT=2525
MAILTRAP_USER=your_mailtrap_username
MAILTRAP_PASS=your_mailtrap_password
MAILTRAP_FROM=no-reply@pets.dev
APP_EMAIL_SERVICE_ACTIVE=emailServiceSimulado
```

### Produção
```bash
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your_smtp_username
SMTP_PASS=your_smtp_password
SMTP_FROM=no-reply@pets.com
APP_EMAIL_SERVICE_ACTIVE=emailServiceMailTrap
```

## Uso no Código

### Injeção com Qualifier

```java
@Service
public class VerificacaoListener {
    
    private final EmailService emailService;
    
    public VerificacaoListener(@Qualifier("emailServiceSimulado") EmailService emailService) {
        this.emailService = emailService;
    }
}
```

### Alternando Implementações

Para alternar entre as implementações, modifique a propriedade:

```properties
# Para usar Mailtrap
app.email.service.active=emailServiceMailTrap

# Para usar simulação
app.email.service.active=emailServiceSimulado
```

## Templates Thymeleaf

### Localização
- `src/main/resources/templates/email/verificacao.html`

### Variáveis Disponíveis
- `destinatario`: E-mail do destinatário
- `token`: Código de verificação
- `horasRestantes`: Tempo restante para expiração
- `appName`: Nome da aplicação

## Endpoints de Teste

### Informações do Serviço
```http
GET /emails/info
```

### Teste de E-mail Simples
```http
POST /emails/test?to=teste@exemplo.com
```

### Teste de E-mail HTML
```http
POST /emails/test/html?to=teste@exemplo.com
```

## Configuração Mailtrap

### 1. Criar Conta
- Acesse [mailtrap.io](https://mailtrap.io)
- Crie uma conta gratuita

### 2. Configurar Sandbox
- Crie um novo Inbox
- Copie as credenciais SMTP:
  - Host
  - Porta
  - Usuário
  - Senha

### 3. Configurar Variáveis
```bash
export MAILTRAP_HOST=sandbox.smtp.mailtrap.io
export MAILTRAP_PORT=2525
export MAILTRAP_USER=seu_usuario
export MAILTRAP_PASS=sua_senha
export MAILTRAP_FROM=no-reply@pets.dev
```

### 4. Testar Conexão
- A aplicação testa a conexão SMTP na inicialização
- Verifique os logs para confirmar sucesso

## Monitoramento e Logs

### Logs de Desenvolvimento
```
[EMAIL] Iniciando envio de e-mail de verificação para: usuario@exemplo.com
[EMAIL] E-mail enviado com sucesso para: usuario@exemplo.com
```

### Logs de Produção (Mailtrap)
```
[MAILTRAP] Enviando e-mail de verificação para: usuario@exemplo.com
[MAILTRAP] E-mail de verificação enviado com sucesso para: usuario@exemplo.com
```

### Health Check
- A implementação Mailtrap testa a conexão SMTP na inicialização
- Falhas de conexão são logadas mas não impedem a inicialização da aplicação

## Tratamento de Erros

### Exceções Específicas
- `MailAuthenticationException`: Falha de autenticação SMTP
- `MailSendException`: Falha no envio SMTP
- Exceções genéricas são convertidas para `RuntimeException`

### Fallback
- Se o template Thymeleaf falhar, usa HTML padrão
- Logs detalhados para debugging

## Segurança

### Boas Práticas
- Credenciais via variáveis de ambiente
- Timeouts configurados para evitar travamento
- Encoding UTF-8 para caracteres especiais
- TLS habilitado para comunicação segura

### Configurações de Timeout
- **Dev**: 5 segundos (conexão, leitura, escrita)
- **Prod**: 10 segundos (conexão, leitura, escrita)

## Migração para Produção

### 1. Verificar Domínio
- Configure SPF/DKIM/DMARC se necessário
- Use domínio verificado para o campo "from"

### 2. Configurar Provedor
- Mailtrap Sending ou outro provedor SMTP
- Teste com caixa de e-mail real

### 3. Atualizar Configurações
- Modifique `application-prod.properties`
- Configure variáveis de ambiente de produção

### 4. Testar
- Use endpoints de teste
- Verifique logs e métricas
- Confirme entrega de e-mails

## Troubleshooting

### Problemas Comuns

#### Falha de Autenticação
```
[MAILTRAP] Falha ao testar conexão SMTP: Authentication failed
```
**Solução**: Verifique usuário e senha do Mailtrap

#### Falha de Conexão
```
[MAILTRAP] Falha ao testar conexão SMTP: Connection refused
```
**Solução**: Verifique host e porta SMTP

#### Timeout
```
[MAILTRAP] Falha ao testar conexão SMTP: Connection timed out
```
**Solução**: Verifique firewall e configurações de rede

### Debug
- Habilite logs DEBUG para `org.springframework.mail`
- Verifique configurações SMTP no log de inicialização
- Use endpoints de teste para validar funcionalidade

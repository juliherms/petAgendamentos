# Docker Compose + MySQL - Configuração para CI/CD

## Visão Geral

Os workflows do GitHub Actions agora incluem **MySQL via Docker Compose** para executar testes com um banco de dados real, garantindo que os testes sejam mais próximos do ambiente de produção.

## Configuração do Docker Compose

### Arquivo `docker-compose.yml`
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: pets-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: pets
      MYSQL_USER: pets_user
      MYSQL_PASSWORD: pets_pass
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    command: --default-authentication-plugin=mysql_native_password
    restart: unless-stopped

volumes:
  mysql_data:
```

### Configuração de Teste MySQL
Arquivo: `src/main/resources/application-test-mysql.properties`

```properties
# Configuração para testes com MySQL (Docker Compose)
spring.datasource.url=jdbc:mysql://localhost:3306/pets?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=pets_user
spring.datasource.password=pets_pass
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# Configurações JPA para testes
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Configurações de conexão
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# Configurações específicas para testes
spring.test.database.replace=none
spring.jpa.defer-datasource-initialization=true
```

## Como Funciona nos Workflows

### 1. Inicialização do MySQL
```yaml
- name: Start MySQL with Docker Compose
  run: |
    docker-compose up -d mysql
    # Aguarda MySQL estar pronto
    timeout 60 bash -c 'until docker exec pets-mysql mysqladmin ping -h"localhost" -u"pets_user" -p"pets_pass" --silent; do sleep 2; done'
    echo "MySQL is ready!"
```

### 2. Verificação do Status
```yaml
- name: Show MySQL status
  run: |
    docker-compose ps
    docker exec pets-mysql mysql -u pets_user -ppets_pass -e "SHOW DATABASES;"
```

### 3. Execução dos Testes
```yaml
- name: Build & run tests
  run: |
    if [ -f ./mvnw ]; then MVN=./mvnw; else MVN=mvn; fi
    $MVN -B -ntp -U clean verify -Dspring.profiles.active=test-mysql
```

## Vantagens do MySQL nos Testes

### ✅ **Benefícios**
- **Testes mais realistas**: Usa o mesmo banco que produção
- **Validação de SQL**: Testa queries reais do MySQL
- **Configurações de conexão**: Valida pool de conexões
- **Transações**: Testa comportamento real de transações
- **Índices e constraints**: Valida estrutura do banco

### ⚠️ **Considerações**
- **Tempo de execução**: MySQL leva tempo para inicializar
- **Recursos**: Consome mais memória e CPU
- **Dependências**: Requer Docker disponível no runner

## Perfis de Teste

### Perfil `test` (H2 - Local)
```properties
# src/main/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
```

### Perfil `test-mysql` (MySQL - CI/CD)
```properties
# src/main/resources/application-test-mysql.properties
spring.datasource.url=jdbc:mysql://localhost:3306/pets
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

## Comandos Locais

### Testar com H2 (Rápido)
```bash
./mvnw clean test -Dspring.profiles.active=test
```

### Testar com MySQL (Realista)
```bash
# Iniciar MySQL
docker-compose up -d mysql

# Aguardar MySQL estar pronto
timeout 60 bash -c 'until docker exec pets-mysql mysqladmin ping -h"localhost" -u"pets_user" -p"pets_pass" --silent; do sleep 2; done'

# Executar testes
./mvnw clean test -Dspring.profiles.active=test-mysql
```

## Solução de Problemas

### MySQL não inicia
```bash
# Verificar logs
docker-compose logs mysql

# Verificar se a porta está livre
netstat -an | grep 3306

# Reiniciar container
docker-compose restart mysql
```

### Timeout na conexão
- Aumentar `timeout-minutes` no workflow
- Verificar se o MySQL está respondendo
- Ajustar configurações de pool de conexões

### Erro de autenticação
- Verificar credenciais no `docker-compose.yml`
- Confirmar se o usuário foi criado
- Verificar permissões do banco

## Próximos Passos

1. **Teste local**: Execute testes com MySQL localmente
2. **Monitoramento**: Acompanhe o tempo de execução dos workflows
3. **Otimização**: Ajuste configurações de pool e timeout conforme necessário
4. **Expansão**: Adicione outros serviços (Redis, PostgreSQL) se necessário

## Recursos Adicionais

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [MySQL Docker Image](https://hub.docker.com/_/mysql)
- [Spring Boot Database Configuration](https://spring.io/guides/gs/accessing-data-mysql/)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)

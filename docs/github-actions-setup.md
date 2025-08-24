# GitHub Actions - Configuração e Uso

## Visão Geral

Este projeto está configurado com um GitHub Actions que executa automaticamente os testes a cada push e pull request.

## Workflow Configurado

O workflow está localizado em `.github/workflows/tests.yml` e executa:

- **Trigger**: Push em qualquer branch e Pull Requests
- **Java**: Versão 21 (Temurin)
- **Maven**: Com cache automático
- **Comando**: `mvn clean verify` (inclui testes unitários)
- **Timeout**: 20 minutos
- **Artefatos**: Relatórios de teste sempre disponíveis

## Como Funciona

1. **Push/Pull Request**: O workflow é disparado automaticamente
2. **Setup**: Instala Java 21 e configura Maven com cache
3. **Execução**: Roda `mvn clean verify` usando o wrapper do projeto
4. **Relatórios**: Faz upload dos relatórios de teste como artefato
5. **Concorrência**: Cancela execuções anteriores do mesmo branch

## Configurações do Projeto

### Java 21
- Configurado no `pom.xml` com `<java.version>21</java.version>`
- Usa Spring Boot 3.5.5 (compatível com Java 21)

### Banco de Teste
- **MySQL via Docker Compose**: Para testes de integração e CI/CD
- **H2 em memória**: Para testes unitários locais
- **Perfil MySQL**: `application-test-mysql.properties` para CI/CD
- **Perfil H2**: `application-test.properties` para testes locais

### Maven Wrapper
- Projeto já possui `mvnw` e `mvnw.cmd`
- Evita problemas de versão do Maven no CI

## Verificação

Para verificar se está funcionando:

1. Faça um push para qualquer branch
2. Vá para a aba "Actions" no GitHub
3. O workflow "CI - Maven Tests (Java 21)" deve aparecer
4. Clique para ver os logs de execução

## Solução de Problemas

### Falha nos Testes
- Baixe o artefato "maven-test-reports" para ver detalhes
- Verifique os logs do workflow para erros de compilação

### Erro de Permissão do Maven Wrapper
- **Sintoma**: `./mvnw: Permission denied`
- **Solução**: O workflow já inclui `chmod +x ./mvnw` para corrigir permissões
- **Causa**: Arquivos do Git não preservam permissões de execução no Windows

### Timeout
- O workflow tem timeout de 20 minutos
- Se precisar de mais tempo, ajuste `timeout-minutes` no YAML

### Cache do Maven
- O cache é automático e otimiza builds subsequentes
- Se houver problemas, pode ser limpo manualmente

## Personalizações Possíveis

### Adicionar Cobertura (JaCoCo)
```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.12</version>
  <!-- configurações... -->
</plugin>
```

### Testes de Integração
- Adicionar `maven-failsafe-plugin` no `pom.xml`
- Configurar `-DskipITs` para pular em PRs se necessário

### Serviços Externos
- **MySQL via Docker Compose**: ✅ Já configurado nos workflows
- **Configuração**: Usa `docker-compose.yml` do projeto
- **Perfil**: `test-mysql` para testes com banco real
- **Outros**: Pode adicionar PostgreSQL, Redis, etc. conforme necessário

## Comandos Locais

Para testar localmente o que o CI fará:

```bash
# Usando wrapper do Maven
./mvnw clean verify

# Ou com Maven local
mvn clean verify
```

## Próximos Passos

1. **Primeiro Push**: Faça um commit e push para testar o workflow
2. **Monitoramento**: Acompanhe a aba Actions para garantir funcionamento
3. **Otimizações**: Ajuste timeout ou adicione funcionalidades conforme necessário

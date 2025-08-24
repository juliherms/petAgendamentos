# Configuração JaCoCo para Cobertura de Código

## Visão Geral

Para usar o workflow avançado com cobertura de código, você precisa adicionar o plugin JaCoCo ao seu `pom.xml`.

## Configuração no pom.xml

Adicione o seguinte plugin na seção `<build><plugins>` do seu `pom.xml`:

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.11</version>
  <executions>
    <execution>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>test</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

**Nota**: Esta é uma configuração básica que gera relatórios automaticamente após os testes. Para thresholds de cobertura, veja a seção "Configurações Avançadas" abaixo.

## O que cada execução faz

1. **prepare-agent**: Prepara o agente JaCoCo para coletar dados de cobertura
2. **report**: Gera o relatório HTML de cobertura automaticamente após os testes

## Configurações Avançadas

### Threshold de Cobertura (Opcional)
Para verificar se a cobertura está acima de um mínimo, adicione esta execução:

```xml
<execution>
  <id>check</id>
  <goals>
    <goal>check</goal>
  </goals>
  <configuration>
    <rules>
      <rule>
        <element>BUNDLE</element>
        <limits>
          <limit>
            <counter>LINE</counter>
            <value>COVEREDRATIO</value>
            <minimum>0.70</minimum>
          </limit>
        </limits>
      </rule>
    </rules>
  </configuration>
</execution>
```

## Configurações Personalizáveis

### Threshold de Cobertura
Altere o valor `<minimum>0.70</minimum>` para o percentual desejado:
- `0.70` = 70%
- `0.80` = 80%
- `0.90` = 90%

### Tipos de Cobertura
Você pode configurar diferentes tipos de cobertura:

```xml
<limit>
  <counter>BRANCH</counter>
  <value>COVEREDRATIO</value>
  <minimum>0.60</minimum>
</limit>
<limit>
  <counter>CLASS</counter>
  <value>MISSEDCOUNT</value>
  <maximum>0</maximum>
</limit>
```

## Comandos Locais

Para testar a cobertura localmente:

```bash
# Executar testes e gerar relatório de cobertura
./mvnw clean test

# Build completo com cobertura
./mvnw clean verify

# Apenas gerar relatório (se testes já foram executados)
./mvnw jacoco:report

# Verificar thresholds (se configurado)
./mvnw jacoco:check
```

## Relatórios Gerados

Após a execução, os relatórios estarão disponíveis em:
- `target/site/jacoco/index.html` - Relatório principal
- `target/site/jacoco/` - Diretório completo com detalhes

## Integração com GitHub Actions

O workflow avançado (`tests-advanced.yml`) automaticamente:
1. Executa os testes com cobertura
2. Gera o relatório JaCoCo
3. Faz upload como artefato
4. Comenta no PR com o percentual de cobertura

## Exclusões

Para excluir classes específicas da cobertura:

```xml
<configuration>
  <excludes>
    <exclude>**/config/**</exclude>
    <exclude>**/dto/**</exclude>
    <exclude>**/Application.java</exclude>
  </excludes>
</configuration>
```

## Próximos Passos

1. Adicione o plugin JaCoCo ao `pom.xml`
2. Teste localmente com `./mvnw jacoco:report`
3. Use o workflow avançado para CI/CD com cobertura
4. Monitore e melhore a cobertura gradualmente

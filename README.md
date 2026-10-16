# QA Crédito API

Projeto educacional de automação de testes de uma API fictícia de análise de crédito, utilizando Java, RestAssured, JUnit 5 e Gradle.

A aplicação e suas regras não representam políticas reais de concessão de crédito e não possuem vínculo com empresas.

## Objetivo

Demonstrar a transformação de requisitos em testes automatizados, com validação de regras de negócio, valores de fronteira, entradas inválidas e respostas HTTP.

## Tecnologias

| Tecnologia | Finalidade |
| --- | --- |
| Java 17 | Implementação da API, das regras de negócio e dos testes. |
| Gradle Wrapper | Gerenciamento de dependências, compilação e execução dos testes. |
| JUnit 5 | Organização dos testes, parametrização de cenários e validações. |
| RestAssured | Envio de requisições HTTP e validação das respostas da API. |
| Gson | Leitura e geração de JSON na API. |
| HttpServer do JDK | Disponibilização do servidor HTTP local. |

## Funcionalidades

- Consulta de disponibilidade da API.
- Análise de crédito com critérios fictícios de idade, renda mensal e valor solicitado.
- Retorno dos motivos de reprovação.
- Validação dos campos obrigatórios, tipos de dados e valores permitidos.
- Tratamento de JSON malformado, métodos não permitidos e Content-Type incorreto.

## Organização e responsabilidade das classes

O código está separado em implementação (`src/main/java`) e testes automatizados (`src/test/java`), utilizando o pacote `credito`.

### Implementação

Localização: `src/main/java/credito`.

| Classe | Responsabilidade |
| --- | --- |
| `AnaliseCreditoService` | Validar os dados de entrada e aplicar as regras fictícias de aprovação de crédito. Retorna o status da análise e os motivos de reprovação. |
| `CreditoApi` | Disponibilizar os endpoints HTTP, interpretar o JSON recebido, chamar o serviço de análise e gerar as respostas HTTP. Permite iniciar a API manualmente pelo método `main`. |

O record `Resultado`, declarado dentro de `AnaliseCreditoService`, representa o retorno da análise, contendo `status` e `motivos`.

Os valores monetários são tratados com `BigDecimal`, evitando imprecisões de representação associadas ao uso de `double`.

### Testes automatizados

Localização: `src/test/java/credito`.

| Classe | Responsabilidade | Casos |
| --- | --- | ---: |
| `AnaliseCreditoServiceTest` | Testar diretamente as regras de negócio, sem requisições HTTP. Contempla aprovação, reprovação, valores de fronteira, campos ausentes e valores inválidos. | 19 |
| `AnaliseCreditoApiTest` | Testar a API com RestAssured, verificando códigos HTTP, cabeçalhos, mensagens e resultados da análise. Inicia e encerra uma instância de `CreditoApi` automaticamente. | 8 |
| `ApiDisponibilidadeTest` | Verificar a integração inicial entre Java, JUnit e RestAssured, utilizando um servidor HTTP mínimo criado dentro do próprio teste. Não utiliza a classe `CreditoApi`. | 1 |

### Fluxo de uma análise

1. O cliente envia um JSON para `POST /analises-credito`.
2. `CreditoApi` interpreta o JSON e verifica os tipos dos campos.
3. `AnaliseCreditoService` valida os valores e aplica as regras de negócio.
4. O serviço retorna o resultado ou sinaliza dados inválidos.
5. `CreditoApi` transforma esse retorno em uma resposta HTTP com JSON.

Essa separação permite testar as regras de negócio isoladamente e verificar sua integração com a comunicação HTTP.

## Estratégia de testes

A suíte possui 28 testes:

| Camada | Quantidade |
| --- | ---: |
| Verificação HTTP inicial do ambiente | 1 |
| Testes unitários das regras de negócio | 19 |
| Testes HTTP da API com RestAssured | 8 |
| **Total** | **28** |

### Cenários contemplados

- Aprovação de uma solicitação válida.
- Aprovação nos valores de fronteira de idade, renda e valor solicitado.
- Reprovação por idade, renda ou valor acima do limite.
- Retorno de todos os motivos quando vários critérios de aprovação falham.
- Rejeição de campos obrigatórios ausentes ou nulos.
- Rejeição de valores fora dos intervalos permitidos.
- Rejeição de valores monetários com mais de duas casas decimais.
- Rejeição de JSON malformado.
- Rejeição de renda enviada como texto.
- Validação de método HTTP não permitido.
- Validação de Content-Type incorreto.

Os testes HTTP iniciam e encerram seus servidores locais automaticamente, utilizando portas disponíveis.

Não é necessário iniciar a aplicação manualmente para executar a suíte.

Nos testes de análise da API, os detalhes das requisições e respostas são exibidos no console quando uma validação falha.

## Pré-requisitos

- JDK 17 instalado.
- `JAVA_HOME` apontando para a pasta principal do JDK.
- Acesso à internet na primeira execução para baixar o Gradle e as dependências.

Não é necessário instalar o Gradle separadamente.

## Executar os testes

Execute os comandos dentro da pasta principal do projeto.

### Windows PowerShell

```powershell
.\gradlew.bat test
```

### Linux ou macOS

```bash
  bash ./gradlew test
```

### Limpar os arquivos gerados e executar os testes

No Windows PowerShell:

```powershell
.\gradlew.bat clean test
```

No Linux ou macOS:

```bash
  bash ./gradlew clean test
```

O resultado esperado é `BUILD SUCCESSFUL`, com os 28 testes aprovados.

## Visualizar o relatório

Após a execução, abra o arquivo:

```text
build/reports/tests/test/index.html
```

No Windows PowerShell:

```powershell
Start-Process .\build\reports\tests\test\index.html
```

O relatório apresenta a quantidade de testes, falhas, testes ignorados, duração e resultados por classe.

O percentual de sucesso representa a proporção de testes aprovados na execução. Ele não representa a cobertura do código.

## Executar a API manualmente

No IntelliJ IDEA:

1. Abra a classe `CreditoApi`, em `src/main/java/credito`.
2. Localize o método `main`.
3. Clique no ícone de execução ao lado do método.
4. Selecione a opção para executar `CreditoApi.main()`.

Endereço local:

```text
http://127.0.0.1:8080
```

Para consultar a disponibilidade, acesse:

```text
http://127.0.0.1:8080/health
```

A aplicação permanece ativa até ser encerrada pelo botão **Stop**.

A rota inicial `/` não possui um endpoint implementado e retorna HTTP 404.

## Endpoints

### Consultar disponibilidade

```http
GET /health
```

Resposta HTTP 200:

```json
{
  "status": "UP"
}
```

### Analisar uma solicitação

```http
POST /analises-credito
Content-Type: application/json
```

Exemplo de entrada:

```json
{
  "idade": 30,
  "rendaMensal": 3000.00,
  "valorSolicitado": 10000.00
}
```

Exemplo de aprovação — HTTP 200:

```json
{
  "status": "APROVADO",
  "motivos": []
}
```

Exemplo de reprovação — HTTP 200:

```json
{
  "status": "REPROVADO",
  "motivos": [
    "Solicitante deve ter pelo menos 18 anos."
  ]
}
```

Uma análise reprovada também retorna HTTP 200, pois a solicitação foi processada corretamente e não atendeu aos critérios de aprovação.

Exemplo de dados obrigatórios ausentes — HTTP 400:

```json
{
  "erro": "Idade, renda mensal e valor solicitado são obrigatórios."
}
```

## Respostas HTTP

| Código | Situação |
| --- | --- |
| 200 | Consulta ou análise realizada, incluindo análises reprovadas. |
| 400 | Dados de entrada ou JSON inválidos. |
| 404 | Endpoint não encontrado. |
| 405 | Método HTTP não permitido para o endpoint. |
| 415 | Content-Type não aceito. |

## Regras de negócio

Consulte a documentação em [Regras de negócio](docs/regras-de-negocio.md).

## Limites do projeto

- Aplicação educacional executada localmente.
- Regras de crédito fictícias.
- Sem autenticação e sem persistência em banco de dados.
- Não utiliza dados reais de clientes.
- Não contempla requisitos de uma aplicação de produção.
- A aprovação dos testes não significa cobertura total do código ou ausência de defeitos.

## Autora

Letícia Gonçalves Garcia — QA Sênior

[Portfólio](https://leticiagbrito.github.io/)
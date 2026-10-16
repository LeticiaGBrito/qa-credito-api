# QA Crédito API — Java

[![Testes e deploy](https://github.com/LeticiaGBrito/qa-credito-api/actions/workflows/testes.yml/badge.svg)](https://github.com/LeticiaGBrito/qa-credito-api/actions/workflows/testes.yml)

Projeto educacional de automação de testes de uma API fictícia de análise de crédito, utilizando Java, RestAssured, JUnit 5 e Gradle, com documentação Swagger, execução de testes no GitHub Actions e publicação no Render com Docker.

A aplicação e suas regras não representam políticas reais de concessão de crédito e não possuem vínculo com empresas.

## Objetivo

Demonstrar a transformação de requisitos em testes automatizados, contemplando:

- Regras de negócio e motivos de reprovação.
- Valores de fronteira.
- Campos obrigatórios e entradas inválidas.
- Respostas e métodos HTTP.
- Documentação interativa da API.
- Execução automatizada de testes.
- Publicação do commit aprovado nos testes.

## API pública

| Recurso | Endereço |
| --- | --- |
| URL base | https://qa-credito-api.onrender.com |
| Disponibilidade | https://qa-credito-api.onrender.com/health |
| Swagger UI | https://qa-credito-api.onrender.com/docs |
| Especificação OpenAPI | https://qa-credito-api.onrender.com/openapi.json |

A hospedagem utiliza uma instância gratuita do Render, que pode pausar por inatividade. O primeiro acesso após uma pausa pode demorar.

A URL base não possui uma página inicial. Utilize `/docs` para experimentar a API ou `/health` para consultar sua disponibilidade.

Utilize somente dados fictícios nas requisições.

Os endpoints de documentação ficam disponíveis no ambiente público após a publicação da versão que os implementa.

## Tecnologias e finalidade

| Tecnologia | Finalidade |
| --- | --- |
| Java 17 | Implementação da API e dos testes |
| Gradle Wrapper | Gerenciamento de dependências, execução dos testes e empacotamento |
| JUnit 5 | Organização e parametrização dos testes |
| RestAssured | Requisições HTTP e validação das respostas |
| Hamcrest | Asserções sobre respostas, coleções e conteúdo |
| Gson | Leitura e geração de JSON |
| BigDecimal | Representação e cálculo de valores monetários |
| HttpServer do JDK | Disponibilização dos endpoints HTTP |
| OpenAPI 3.0.3 | Especificação dos endpoints, dados e respostas |
| Swagger UI | Interface para consultar a documentação e enviar requisições |
| Docker | Construção da imagem e execução da aplicação em contêiner |
| GitHub Actions | Execução dos testes e solicitação de deploy |
| GitHub Secrets | Armazenamento do endereço secreto do Deploy Hook |
| Render | Hospedagem da API e execução dos deploys |

## Funcionalidades

- Consulta de disponibilidade da API.
- Análise de crédito com critérios fictícios de idade, renda mensal e valor solicitado.
- Retorno dos motivos de reprovação.
- Validação dos campos obrigatórios e dos tipos de dados.
- Tratamento de JSON malformado, métodos não permitidos e Content-Type incorreto.
- Documentação interativa em `/docs`.
- Especificação OpenAPI em `/openapi.json`.

## Organização e responsabilidade das classes

| Classe | Localização | Responsabilidade |
| --- | --- | --- |
| `AnaliseCreditoService` | `src/main/java/credito/` | Validar os dados e aplicar as regras fictícias de análise de crédito |
| `CreditoApi` | `src/main/java/credito/` | Iniciar o servidor HTTP, processar requisições e disponibilizar a documentação |
| `AnaliseCreditoServiceTest` | `src/test/java/credito/` | Testar diretamente as regras de negócio e as validações do serviço |
| `AnaliseCreditoApiTest` | `src/test/java/credito/` | Testar a análise de crédito e as rotas de documentação com RestAssured |
| `ApiDisponibilidadeTest` | `src/test/java/credito/` | Verificar o ambiente de testes HTTP com um servidor local independente |

O resultado da análise é representado pelo record `Resultado`, declarado dentro de `AnaliseCreditoService`, contendo o status e os motivos.

### Outros arquivos importantes

| Arquivo | Finalidade |
| --- | --- |
| `src/main/resources/openapi.json` | Descrever os endpoints, dados de entrada, respostas e exemplos da API |
| `build.gradle` | Configurar dependências, Java 17, testes e empacotamento |
| `Dockerfile` | Executar os testes, gerar o pacote e construir a imagem da aplicação |
| `.dockerignore` | Excluir arquivos desnecessários do contexto de construção da imagem |
| `.github/workflows/testes.yml` | Definir os jobs de testes e solicitação de deploy |
| `docs/regras-de-negocio.md` | Documentar as regras fictícias utilizadas no projeto |
| `.gitignore` | Evitar o versionamento de arquivos locais e resultados de compilação |

A especificação OpenAPI é carregada do classpath. O Gradle inclui o arquivo de `src/main/resources` no empacotamento da aplicação.

## Estratégia de testes

A suíte possui **35 testes**:

| Grupo | Quantidade |
| --- | ---: |
| Verificação HTTP inicial do ambiente | 1 |
| Testes unitários das regras de negócio | 19 |
| Testes HTTP da análise de crédito | 8 |
| Testes HTTP da documentação | 7 |
| **Total** | **35** |

### Regras de negócio

Os testes unitários contemplam:

- Aprovação com dados válidos.
- Valores de fronteira de idade, renda e limite solicitado.
- Reprovação por critérios de negócio.
- Retorno de múltiplos motivos de reprovação.
- Campos obrigatórios e valores inválidos.

### Análise de crédito via HTTP

Os 8 testes verificam:

- Aprovação de solicitação válida.
- Reprovação por idade.
- Rejeição de renda ausente.
- Aprovação nos limites dos critérios.
- Rejeição de JSON malformado.
- Rejeição de renda enviada como texto.
- Método HTTP não permitido.
- Content-Type incorreto.

### Documentação via HTTP

Os 7 testes verificam:

- Disponibilização do HTML do Swagger em `/docs`.
- Acesso à documentação por `/docs/`.
- Especificação OpenAPI com endpoints e campos esperados.
- Rejeição de POST em `/docs`.
- Rejeição de POST em `/openapi.json`.
- Rejeição de subcaminho inexistente em `/docs`.
- Rejeição de sufixo inválido em `/openapi.json`.

Os testes da documentação verificam o HTML retornado e campos selecionados da especificação. Não realizam validação completa do padrão OpenAPI nem executam JavaScript em um navegador.

O carregamento visual do Swagger e o envio de uma solicitação pela interface foram verificados manualmente.

### Ambiente dos testes

Os testes de `AnaliseCreditoApiTest` iniciam e encerram automaticamente uma instância local da API, utilizando uma porta disponível.

O teste de `ApiDisponibilidadeTest` utiliza seu próprio servidor HTTP local.

Não é necessário iniciar a aplicação manualmente para executar a suíte. Os testes não dependem da API publicada no Render nem acessam a CDN do Swagger.

A aprovação dos testes não significa cobertura total do código.

## Pré-requisitos para execução local

- JDK 17 instalado.
- `JAVA_HOME` apontando para a pasta do JDK.
- Acesso à internet na primeira execução para baixar o Gradle e as dependências.

Não é necessário instalar o Gradle separadamente.

Para utilizar a interface Swagger, o navegador precisa acessar a CDN que fornece seus arquivos CSS e JavaScript.

## Executar os testes

No Windows PowerShell, dentro da pasta do projeto:

```powershell
.\gradlew.bat clean test
```

No Linux ou macOS:

```bash
bash ./gradlew clean test
```

## Visualizar o relatório local

Após a execução, abra:

```text
build/reports/tests/test/index.html
```

No Windows PowerShell:

```powershell
Start-Process .\build\reports\tests\test\index.html
```

O relatório apresenta a quantidade de testes, falhas, testes ignorados e duração da execução.

O indicador de sucesso representa a proporção de testes aprovados naquela execução, não a cobertura do código.

## Executar a API localmente

No Windows PowerShell:

```powershell
.\gradlew.bat run
```

No Linux ou macOS:

```bash
bash ./gradlew run
```

Também é possível abrir a classe `CreditoApi` no IntelliJ IDEA e executar seu método `main`.

| Recurso local | Endereço |
| --- | --- |
| Disponibilidade | http://127.0.0.1:8080/health |
| Swagger UI | http://127.0.0.1:8080/docs |
| Especificação OpenAPI | http://127.0.0.1:8080/openapi.json |

O comando `run` permanece em execução enquanto o servidor estiver ativo. O Gradle pode exibir `EXECUTING` durante esse período.

Para encerrar pelo terminal, pressione `Ctrl + C`.

### Gerar e executar o pacote

No Windows PowerShell:

```powershell
.\gradlew.bat installDist
```

Depois:

```powershell
.\build\install\credito-api\bin\credito-api.bat
```

Esse pacote permite executar a aplicação sem iniciá-la pela IDE. Java 17 continua sendo necessário no ambiente.

## Configuração de host e porta

A aplicação lê as seguintes variáveis de ambiente:

| Variável | Valor padrão da aplicação | Finalidade |
| --- | --- | --- |
| `HOST` | `127.0.0.1` | Interface de rede em que o servidor recebe requisições |
| `PORT` | `8080` | Porta utilizada pelo servidor |

No Dockerfile, `HOST` é definido como `0.0.0.0`, permitindo receber requisições externas ao contêiner.

No Render, a aplicação utiliza a variável `PORT` fornecida pela plataforma.

## Experimentar a API pelo Swagger

Com a aplicação local em execução, acesse:

http://127.0.0.1:8080/docs

Após a publicação da versão com Swagger, também é possível acessar:

https://qa-credito-api.onrender.com/docs

Para realizar uma análise:

1. Expanda `POST /analises-credito`.
2. Clique em `Try it out`.
3. Selecione um exemplo ou edite o JSON.
4. Clique em `Execute`.
5. Consulte o código HTTP e o corpo em `Server response`.

A interface apresenta exemplos de:

- Solicitação aprovada.
- Aprovação nos limites dos critérios.
- Reprovação por idade.
- Reprovação por múltiplos critérios.

Os exemplos da documentação são ilustrativos. A seção `Server response`, após o envio, apresenta a resposta efetiva da API.

### Como a documentação funciona

- `/docs` retorna a página HTML da interface.
- O navegador carrega Swagger UI pela CDN, com versão fixada em `5.32.0`.
- Swagger UI consulta `/openapi.json` na própria aplicação.
- O endereço relativo `/` da especificação permite enviar requisições ao servidor atual, localmente ou no Render.
- A validação dos dados continua sendo realizada pelo código Java.

A especificação é mantida manualmente e deve ser atualizada quando o comportamento da API mudar.

## Endpoints e exemplos de uso

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

Exemplo no Windows PowerShell:

```powershell
Invoke-RestMethod `
  -Uri "https://qa-credito-api.onrender.com/health" `
  -Method Get
```

Esse endpoint informa disponibilidade da aplicação. Não verifica banco de dados ou serviços externos.

### Analisar uma solicitação de crédito

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

Exemplo de resposta HTTP 200:

```json
{
  "status": "APROVADO",
  "motivos": []
}
```

Exemplo no Windows PowerShell:

```powershell
$corpo = @{
    idade = 30
    rendaMensal = 3000
    valorSolicitado = 10000
} | ConvertTo-Json

$resposta = Invoke-RestMethod `
    -Uri "https://qa-credito-api.onrender.com/analises-credito" `
    -Method Post `
    -ContentType "application/json" `
    -Body $corpo

$resposta | ConvertTo-Json -Depth 5
```

Para executar os mesmos exemplos localmente, substitua a URL base por:

```text
http://127.0.0.1:8080
```

### Exemplo de reprovação

Entrada:

```json
{
  "idade": 17,
  "rendaMensal": 3000.00,
  "valorSolicitado": 10000.00
}
```

Resposta HTTP 200:

```json
{
  "status": "REPROVADO",
  "motivos": [
    "Solicitante deve ter pelo menos 18 anos."
  ]
}
```

Uma reprovação de crédito retorna HTTP 200 porque a análise foi realizada com sucesso. O resultado da regra de negócio é informado no campo `status`.

Dados de entrada inválidos, como campos obrigatórios ausentes, retornam HTTP 400.

### Consultar a documentação

```http
GET /docs
```

Retorna a página HTML do Swagger UI.

O caminho `/docs/` também é aceito.

### Consultar a especificação

```http
GET /openapi.json
```

Retorna a especificação OpenAPI em JSON.

## Respostas HTTP

| Código | Situação |
| --- | --- |
| 200 | Consulta, análise ou documentação disponibilizada |
| 400 | Dados de entrada inválidos ou JSON malformado |
| 404 | Endpoint não encontrado |
| 405 | Método HTTP não permitido |
| 415 | Content-Type não aceito |
| 500 | Falha de configuração caso o arquivo OpenAPI não seja encontrado |

## Integração e implantação contínuas — CI/CD

O workflow está definido em:

```text
.github/workflows/testes.yml
```

### Integração contínua — CI

O GitHub Actions executa os testes:

- A cada envio de código para uma branch.
- Na abertura ou atualização de pull requests direcionados à `main`.
- Por execução manual em `Run workflow`.

O job de testes:

1. Obtém o código do commit.
2. Configura o Java 17.
3. Executa `clean test` com o Gradle Wrapper.
4. Disponibiliza os relatórios como artefatos da execução.

### Implantação contínua — CD

Após o sucesso do job de testes, o job de deploy pode ser executado quando o evento é um envio para `main` ou uma execução manual nessa branch.

O fluxo é:

1. Os testes são aprovados no GitHub Actions.
2. O workflow consulta o segredo `RENDER_DEPLOY_HOOK_URL`.
3. Uma requisição ao Deploy Hook solicita a publicação.
4. O parâmetro `ref` informa o SHA do commit aprovado nos testes.
5. O Render constrói a imagem Docker desse commit.
6. A aplicação é iniciada e verificada pelo endpoint `/health`.

Se o job de testes falhar, o job de deploy não é executado.

Pull requests e envios para outras branches executam os testes, mas não disparam a publicação.

O Auto-Deploy do Render permanece em `Off`, pois a solicitação de publicação é feita pelo GitHub Actions.

### Como interpretar os resultados

| Indicador | Significado |
| --- | --- |
| Job de testes verde | A execução dos testes terminou com sucesso |
| Job de deploy verde | O Render aceitou a solicitação de publicação |
| Render: `Deploy succeeded` e `Live` | A publicação foi concluída |
| `/health` retornando `UP` | A instância acessada está respondendo |
| `/docs` carregando e enviando requisições | A documentação interativa está acessível no ambiente consultado |

O workflow atual não aguarda a conclusão do deploy no Render. Essa confirmação é realizada no painel da plataforma.

O endpoint `/health` informa disponibilidade, mas não identifica a versão publicada. Para conferir o commit, consulte o campo `Source` do deploy no Render.

Os testes automatizados executam antes da publicação, contra uma instância local da API no ambiente do GitHub Actions. Não há uma suíte funcional automática após o deploy público.

O selo deste README mostra o resultado do workflow, não monitora a disponibilidade pública da aplicação.

### Consultar os relatórios no GitHub

1. Abra a aba `Actions` do repositório.
2. Selecione uma execução.
3. Na seção `Artifacts`, baixe `relatorios-de-testes`.
4. Extraia o arquivo e abra o relatório HTML.

Os artefatos estão configurados para retenção de 14 dias.

### Configuração para reproduzir o deploy

Para publicar uma cópia deste projeto:

1. Crie um Web Service no Render conectado ao seu repositório.
2. Selecione Docker como ambiente de execução.
3. Utilize o `Dockerfile` da raiz do projeto.
4. Configure o Health Check Path como `/health`.
5. Mantenha o Auto-Deploy em `Off`.
6. Realize a primeira publicação pelo painel.
7. Copie o Deploy Hook nas configurações do serviço.
8. No GitHub, cadastre um repository secret chamado `RENDER_DEPLOY_HOOK_URL`.

O endereço do Deploy Hook deve ser mantido em segredo e não deve ser incluído no código, na documentação ou em imagens públicas.

## Construção com Docker

O Dockerfile utiliza duas etapas:

- **Build:** utiliza o JDK 17 para executar os testes e gerar a distribuição com `installDist`.
- **Execução:** utiliza o JRE 17 e copia somente os arquivos necessários para iniciar a API.

A especificação OpenAPI acompanha os recursos empacotados da aplicação.

A aplicação é executada no contêiner com um usuário sem privilégios de administrador.

O Render realiza a construção da imagem a partir do repositório, sem exigir Docker instalado na máquina de quem configura a publicação.

## Regras de negócio

Critérios fictícios de aprovação:

- Idade igual ou superior a 18 anos.
- Renda mensal igual ou superior a R$ 2.000,00.
- Valor solicitado menor ou igual a cinco vezes a renda mensal.

Os dados devem ser válidos antes da aplicação desses critérios.

Consulte a documentação completa em [Regras de negócio](docs/regras-de-negocio.md).

## Limites do projeto

- Aplicação educacional com regras fictícias.
- Sem autenticação e sem persistência em banco de dados.
- Não utiliza dados reais de clientes.
- Os testes automatizados executam contra servidores locais.
- Os testes aprovados não representam cobertura total do código.
- Não inclui testes automatizados de navegador para o Swagger.
- Os testes da especificação verificam campos selecionados, não a conformidade completa com OpenAPI.
- A documentação OpenAPI é mantida manualmente.
- A interface Swagger depende do acesso à CDN.
- A hospedagem gratuita pode apresentar demora após períodos de inatividade.
- O workflow solicita o deploy, mas não acompanha automaticamente seu resultado final.
- Não possui suíte funcional automática após a publicação.
- O projeto não representa uma solução pronta para uso em produção.

## Autora

Letícia Gonçalves Garcia — QA Sênior

[Portfólio](https://leticiagbrito.github.io/) · [GitHub](https://github.com/LeticiaGBrito)
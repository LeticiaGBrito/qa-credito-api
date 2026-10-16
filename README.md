# QA Crédito API

[![Testes e deploy](https://github.com/LeticiaGBrito/qa-credito-api/actions/workflows/testes.yml/badge.svg)](https://github.com/LeticiaGBrito/qa-credito-api/actions/workflows/testes.yml)

Projeto educacional de automação de testes de uma API fictícia de análise de crédito, utilizando Java, RestAssured, JUnit 5 e Gradle, com execução de testes no GitHub Actions e publicação no Render com Docker.

A aplicação e suas regras não representam políticas reais de concessão de crédito e não possuem vínculo com empresas.

## Objetivo

Demonstrar a transformação de requisitos em testes automatizados, contemplando:

- Regras de negócio e motivos de reprovação.
- Valores de fronteira.
- Campos obrigatórios e entradas inválidas.
- Respostas e métodos HTTP.
- Execução automatizada de testes.
- Publicação do commit aprovado nos testes.

## API publicada

**URL base:** https://qa-credito-api.onrender.com

**Disponibilidade:** https://qa-credito-api.onrender.com/health

A API está hospedada em uma instância gratuita do Render, que pode pausar por inatividade. O primeiro acesso após uma pausa pode demorar.

A URL base não possui uma página inicial. Para verificar a disponibilidade, utilize `/health`.

Utilize somente dados fictícios nas requisições.

## Tecnologias e finalidade

| Tecnologia | Finalidade |
| --- | --- |
| Java 17 | Implementação da API e dos testes |
| Gradle Wrapper | Gerenciamento de dependências, execução dos testes e empacotamento |
| JUnit 5 | Organização dos testes, parametrização e asserções |
| RestAssured | Requisições HTTP e validação das respostas |
| Gson | Leitura e geração de JSON |
| HttpServer do JDK | Disponibilização dos endpoints HTTP |
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

## Organização e responsabilidade das classes

| Classe | Localização | Responsabilidade |
| --- | --- | --- |
| `AnaliseCreditoService` | `src/main/java/credito/` | Validar os dados e aplicar as regras fictícias de análise de crédito |
| `CreditoApi` | `src/main/java/credito/` | Iniciar o servidor HTTP, receber as requisições, interpretar o JSON e retornar as respostas |
| `AnaliseCreditoServiceTest` | `src/test/java/credito/` | Testar diretamente as regras de negócio e as validações do serviço |
| `AnaliseCreditoApiTest` | `src/test/java/credito/` | Testar os endpoints e as respostas HTTP com RestAssured |
| `ApiDisponibilidadeTest` | `src/test/java/credito/` | Verificar o ambiente de testes HTTP com um servidor local independente |

O resultado da análise é representado pelo record `Resultado`, declarado dentro de `AnaliseCreditoService`, contendo o status e os motivos.

### Outros arquivos importantes

| Arquivo | Finalidade |
| --- | --- |
| `build.gradle` | Configurar dependências, Java 17, testes e empacotamento |
| `Dockerfile` | Executar os testes, gerar o pacote e construir a imagem da aplicação |
| `.dockerignore` | Excluir arquivos desnecessários do contexto de construção da imagem |
| `.github/workflows/testes.yml` | Definir os jobs de testes e solicitação de deploy |
| `docs/regras-de-negocio.md` | Documentar as regras fictícias utilizadas no projeto |
| `.gitignore` | Evitar o versionamento de arquivos locais e resultados de compilação |

## Estratégia de testes

A suíte possui 28 testes:

| Camada | Quantidade |
| --- | ---: |
| Verificação HTTP inicial do ambiente | 1 |
| Testes unitários das regras de negócio | 19 |
| Testes HTTP da API com RestAssured | 8 |
| **Total** | **28** |

Os testes contemplam aprovação, reprovação, fronteiras, entradas inválidas e tratamento de requisições HTTP.

Os testes HTTP de `AnaliseCreditoApiTest` iniciam e encerram automaticamente uma instância local da API, utilizando uma porta disponível.

O teste de `ApiDisponibilidadeTest` utiliza seu próprio servidor HTTP local.

Não é necessário iniciar a aplicação manualmente para executar a suíte. Os testes não dependem da API publicada no Render.

A aprovação dos testes não significa cobertura total do código.

## Pré-requisitos para execução local

- JDK 17 instalado.
- `JAVA_HOME` apontando para a pasta do JDK.
- Acesso à internet na primeira execução para baixar o Gradle e as dependências.

Não é necessário instalar o Gradle separadamente.

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

Endereço local padrão:

```text
http://127.0.0.1:8080
```

Consulta de disponibilidade:

```text
http://127.0.0.1:8080/health
```

Para encerrar a execução pelo terminal, pressione `Ctrl + C`.

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

## Respostas HTTP

| Código | Situação |
| --- | --- |
| 200 | Consulta ou análise realizada |
| 400 | Dados de entrada inválidos ou JSON malformado |
| 404 | Endpoint não encontrado |
| 405 | Método HTTP não permitido |
| 415 | Content-Type não aceito |

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

O workflow atual não aguarda a conclusão do deploy no Render. Essa confirmação é realizada no painel da plataforma.

O endpoint `/health` informa disponibilidade, mas não identifica a versão publicada. Para conferir o commit, consulte o campo `Source` do deploy no Render.

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

A aplicação é executada no contêiner com um usuário sem privilégios de administrador.

O Render realiza a construção da imagem a partir do repositório, sem exigir Docker instalado na máquina de quem configura a publicação.

## Regras de negócio

Consulte a documentação em [Regras de negócio](docs/regras-de-negocio.md).

## Limites do projeto

- Aplicação educacional com regras fictícias.
- Sem autenticação e sem persistência em banco de dados.
- Não utiliza dados reais de clientes.
- Os testes automatizados executam contra servidores locais.
- A hospedagem gratuita pode apresentar demora após períodos de inatividade.
- O workflow solicita o deploy, mas não acompanha automaticamente seu resultado final.
- O projeto não representa uma solução pronta para uso em produção.

## Autora

Letícia Gonçalves Garcia — QA Sênior

[Portfólio](https://leticiagbrito.github.io/) · [GitHub](https://github.com/LeticiaGBrito)
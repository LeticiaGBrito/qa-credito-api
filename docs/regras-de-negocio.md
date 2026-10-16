# API de análise de crédito — Regras de negócio

## Objetivo

Projeto educacional de automação de testes de APIs com Java,
RestAssured, JUnit 5 e Gradle.

A API e as regras são fictícias, sem vínculo com empresas
ou políticas reais de concessão de crédito.

## Endpoint

POST /analises-credito

Content-Type: application/json

## Dados de entrada

- idade: número inteiro entre 0 e 120, inclusive.
- rendaMensal: número maior que zero, com no máximo duas casas decimais.
- valorSolicitado: número maior que zero, com no máximo duas casas decimais.

Todos os campos são obrigatórios e não podem ser nulos.

Os valores devem ser enviados como números JSON, sem aspas.
Valores monetários devem utilizar ponto como separador decimal.

Exemplo de entrada:

    {
      "idade": 30,
      "rendaMensal": 3000.00,
      "valorSolicitado": 10000.00
    }

## Validação dos dados

Antes da análise, a API deve verificar:

- Presença de todos os campos obrigatórios.
- Ausência de valores nulos nos campos obrigatórios.
- Tipos numéricos dos campos.
- Idade inteira entre 0 e 120, inclusive.
- Renda mensal maior que zero.
- Valor solicitado maior que zero.
- Valores monetários com no máximo duas casas decimais.

Dados inválidos devem retornar HTTP 400 e uma mensagem de erro.

A validação dos dados é diferente da avaliação para aprovação:
uma idade de 17 anos é uma entrada válida, mas não atende
ao critério de idade mínima para aprovação.

## Critérios de aprovação

Uma solicitação será aprovada quando atender a todos os critérios:

- Idade igual ou superior a 18 anos.
- Renda mensal igual ou superior a R$ 2.000,00.
- Valor solicitado menor ou igual a cinco vezes a renda mensal.

Os valores exatos dos limites são aceitos.

Exemplo: uma pessoa com 18 anos e renda mensal de R$ 2.000,00
pode ter uma solicitação de R$ 10.000,00 aprovada.

## Resultado da análise

Solicitações com dados válidos devem retornar HTTP 200,
mesmo quando reprovadas.

- APROVADO: todos os critérios de aprovação foram atendidos.
- REPROVADO: pelo menos um critério de aprovação não foi atendido.

O retorno deve conter os campos:

- status: resultado da análise.
- motivos: lista dos motivos de reprovação.

Quando a solicitação for aprovada, a lista de motivos deve estar vazia.

Quando mais de um critério falhar, todos os motivos correspondentes
devem ser retornados.

### Exemplo de aprovação

    {
      "status": "APROVADO",
      "motivos": []
    }

### Exemplo de reprovação

    {
      "status": "REPROVADO",
      "motivos": [
        "Solicitante deve ter pelo menos 18 anos."
      ]
    }

## Motivos de reprovação

| Critério não atendido | Mensagem |
| --- | --- |
| Idade inferior a 18 anos | Solicitante deve ter pelo menos 18 anos. |
| Renda mensal inferior a R$ 2.000,00 | Renda mensal inferior a R$ 2.000,00. |
| Valor solicitado superior a cinco vezes a renda | Valor solicitado superior a cinco vezes a renda mensal. |

## Comportamento HTTP

| Código | Situação |
| --- | --- |
| 200 | Análise realizada, aprovada ou reprovada |
| 400 | Dados inválidos, JSON malformado ou corpo que não seja um objeto JSON |
| 404 | Endpoint não encontrado |
| 405 | Método HTTP não permitido |
| 415 | Content-Type diferente de application/json ou ausente |

Para o endpoint de análise, somente o método POST é permitido.
Uma resposta HTTP 405 deve informar POST no cabeçalho Allow.

## Consulta de disponibilidade

GET /health

Quando a API estiver disponível, deve retornar HTTP 200:

    {
      "status": "UP"
    }

Esse endpoint verifica se a aplicação está respondendo.
Ele não realiza uma análise de crédito nem identifica a versão publicada.

## Principais cenários de teste

1. Aprovar uma solicitação que atende a todos os critérios.
2. Aprovar idade exatamente igual a 18 anos.
3. Aprovar renda exatamente igual a R$ 2.000,00.
4. Aprovar valor solicitado exatamente igual a cinco vezes a renda.
5. Reprovar solicitante com 17 anos.
6. Reprovar renda de R$ 1.999,99.
7. Reprovar valor solicitado acima de cinco vezes a renda.
8. Retornar todos os motivos quando vários critérios falharem.
9. Rejeitar campos obrigatórios ausentes ou nulos.
10. Rejeitar idade negativa ou superior a 120.
11. Rejeitar idade com parte fracionária.
12. Rejeitar renda ou valor solicitado igual a zero ou negativo.
13. Rejeitar valores monetários com mais de duas casas decimais.
14. Rejeitar campos numéricos enviados como texto.
15. Rejeitar JSON malformado.
16. Rejeitar corpo que não seja um objeto JSON.
17. Rejeitar método HTTP não permitido.
18. Rejeitar Content-Type incorreto ou ausente.
19. Consultar a disponibilidade da API.

Essa lista descreve os principais comportamentos esperados.
Ela não representa uma correspondência individual com os 28 testes
automatizados nem afirma que todos esses cenários já estão automatizados.

## Limites das regras

- Não há consulta a dados reais de clientes.
- Não há integração com serviços externos de análise de crédito.
- Não há autenticação nem persistência em banco de dados.
- Os critérios são exclusivamente educacionais.
- Docker e CI/CD não alteram as regras de negócio descritas.
# API de análise de crédito — Regras de negócio

## Objetivo

Projeto educacional de automação de testes de APIs com
Java, RestAssured, JUnit e Gradle.

A API e as regras são fictícias, sem vínculo com empresas
ou políticas reais de concessão de crédito.

## Endpoint

POST /analises-credito

## Dados de entrada

- idade: número inteiro.
- rendaMensal: valor monetário.
- valorSolicitado: valor monetário.

Todos os campos são obrigatórios.

## Validação dos dados

- Idade deve estar entre 0 e 120.
- Renda mensal deve ser maior que zero.
- Valor solicitado deve ser maior que zero.
- Valores monetários devem ter no máximo duas casas decimais.
- Dados inválidos devem retornar HTTP 400 e uma mensagem de erro.

## Critérios de aprovação

Uma solicitação será aprovada quando atender a todos os critérios:

- Idade igual ou superior a 18 anos.
- Renda mensal igual ou superior a R$ 2.000,00.
- Valor solicitado menor ou igual a cinco vezes a renda mensal.

## Resultado da análise

Solicitações válidas devem retornar HTTP 200, mesmo quando reprovadas.

- APROVADO: todos os critérios atendidos.
- REPROVADO: pelo menos um critério não atendido.

O retorno deve informar os motivos da reprovação.

## Cenários iniciais de teste

1. Aprovar uma solicitação que atende a todos os critérios.
2. Aprovar idade exatamente igual a 18 anos.
3. Aprovar renda exatamente igual a R$ 2.000,00.
4. Aprovar valor solicitado exatamente igual a cinco vezes a renda.
5. Reprovar solicitante com 17 anos.
6. Reprovar renda de R$ 1.999,99.
7. Reprovar valor solicitado acima de cinco vezes a renda.
8. Retornar todos os motivos quando vários critérios falharem.
9. Rejeitar campos obrigatórios ausentes ou nulos.
10. Rejeitar valores fora dos intervalos permitidos.
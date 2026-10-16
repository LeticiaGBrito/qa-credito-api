package credito;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AnaliseCreditoApiTest {

    private static CreditoApi api;
    private static String enderecoApi;

    @BeforeAll
    static void iniciarApi() throws IOException {
        // A porta zero permite que o sistema escolha uma porta disponível.
        api = new CreditoApi(0);
        api.iniciar();
        enderecoApi = api.endereco();
    }

    @AfterAll
    static void encerrarApi() {
        if (api != null) {
            api.close();
        }
    }

    // ========================================
    // ANÁLISE DE CRÉDITO
    // ========================================

    @Test
    @DisplayName("Deve aprovar uma solicitação válida via HTTP")
    void deveAprovarSolicitacao() {
        given()
                .baseUri(enderecoApi)
                .contentType("application/json")
                .body("""
                        {
                          "idade": 30,
                          "rendaMensal": 3000.00,
                          "valorSolicitado": 10000.00
                        }
                        """)
                .log().ifValidationFails()
                .when()
                .post("/analises-credito")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json")
                .body("status", equalTo("APROVADO"))
                .body("motivos", empty());
    }

    @Test
    @DisplayName("Deve reprovar menor de idade via HTTP")
    void deveReprovarMenorDeIdade() {
        given()
                .baseUri(enderecoApi)
                .contentType("application/json")
                .body("""
                        {
                          "idade": 17,
                          "rendaMensal": 3000.00,
                          "valorSolicitado": 5000.00
                        }
                        """)
                .log().ifValidationFails()
                .when()
                .post("/analises-credito")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json")
                .body("status", equalTo("REPROVADO"))
                .body("motivos", contains(
                        "Solicitante deve ter pelo menos 18 anos."
                ));
    }

    @Test
    @DisplayName("Deve retornar HTTP 400 quando a renda estiver ausente")
    void deveRejeitarRendaAusente() {
        given()
                .baseUri(enderecoApi)
                .contentType("application/json")
                .body("""
                        {
                          "idade": 30,
                          "valorSolicitado": 5000.00
                        }
                        """)
                .log().ifValidationFails()
                .when()
                .post("/analises-credito")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
                .contentType("application/json")
                .body("erro", equalTo(
                        "Idade, renda mensal e valor solicitado são obrigatórios."
                ));
    }

    @Test
    @DisplayName("Deve aprovar uma solicitação nos limites mínimos e máximos")
    void deveAprovarNosLimites() {
        given()
                .baseUri(enderecoApi)
                .contentType("application/json")
                .body("""
                        {
                          "idade": 18,
                          "rendaMensal": 2000.00,
                          "valorSolicitado": 10000.00
                        }
                        """)
                .log().ifValidationFails()
                .when()
                .post("/analises-credito")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json")
                .body("status", equalTo("APROVADO"))
                .body("motivos", empty());
    }

    @Test
    @DisplayName("Deve retornar HTTP 400 para JSON malformado")
    void deveRejeitarJsonMalformado() {
        given()
                .baseUri(enderecoApi)
                .contentType("application/json")
                .body("{\"idade\": 30,")
                .log().ifValidationFails()
                .when()
                .post("/analises-credito")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
                .contentType("application/json")
                .body("erro", equalTo("JSON inválido."));
    }

    @Test
    @DisplayName("Deve retornar HTTP 400 para renda enviada como texto")
    void deveRejeitarRendaComoTexto() {
        given()
                .baseUri(enderecoApi)
                .contentType("application/json")
                .body("""
                        {
                          "idade": 30,
                          "rendaMensal": "3000.00",
                          "valorSolicitado": 5000.00
                        }
                        """)
                .log().ifValidationFails()
                .when()
                .post("/analises-credito")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
                .contentType("application/json")
                .body("erro", equalTo(
                        "O campo rendaMensal deve ser numérico."
                ));
    }

    @Test
    @DisplayName("Deve retornar HTTP 405 para GET na análise de crédito")
    void deveRejeitarMetodoNaoPermitido() {
        given()
                .baseUri(enderecoApi)
                .log().ifValidationFails()
                .when()
                .get("/analises-credito")
                .then()
                .log().ifValidationFails()
                .statusCode(405)
                .header("Allow", equalTo("POST"))
                .contentType("application/json")
                .body("erro", equalTo(
                        "Método HTTP não permitido."
                ));
    }

    @Test
    @DisplayName("Deve retornar HTTP 415 para conteúdo enviado como texto")
    void deveRejeitarContentTypeIncorreto() {
        given()
                .baseUri(enderecoApi)
                .contentType("text/plain")
                .body("""
                        {
                          "idade": 30,
                          "rendaMensal": 3000.00,
                          "valorSolicitado": 5000.00
                        }
                        """)
                .log().ifValidationFails()
                .when()
                .post("/analises-credito")
                .then()
                .log().ifValidationFails()
                .statusCode(415)
                .contentType("application/json")
                .body("erro", equalTo(
                        "Utilize Content-Type application/json."
                ));
    }

    // ========================================
    // DOCUMENTAÇÃO SWAGGER E OPENAPI
    // ========================================

    @Test
    @DisplayName("Deve disponibilizar o HTML do Swagger em /docs")
    void deveDisponibilizarSwagger() {
        given()
                .baseUri(enderecoApi)
                .log().ifValidationFails()
                .when()
                .get("/docs")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("text/html")
                .body(containsString("SwaggerUIBundle"))
                .body(containsString("id=\"swagger-ui\""))
                .body(containsString("url: \"/openapi.json\""));
    }

    @Test
    @DisplayName("Deve disponibilizar o Swagger com barra final em /docs/")
    void deveDisponibilizarSwaggerComBarraFinal() {
        given()
                .baseUri(enderecoApi)
                .log().ifValidationFails()
                .when()
                .get("/docs/")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("text/html")
                .body(containsString("SwaggerUIBundle"))
                .body(containsString("url: \"/openapi.json\""));
    }

    @Test
    @DisplayName("Deve disponibilizar a especificação OpenAPI com os endpoints")
    void deveDisponibilizarEspecificacaoOpenApi() {
        given()
                .baseUri(enderecoApi)
                .log().ifValidationFails()
                .when()
                .get("/openapi.json")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType("application/json")
                .body("openapi", equalTo("3.0.3"))
                .body("info.title", equalTo("QA Crédito API — Java"))
                .body("servers[0].url", equalTo("/"))
                .body("paths", hasKey("/health"))
                .body("paths", hasKey("/analises-credito"))
                .body("paths.'/health'", hasKey("get"))
                .body("paths.'/analises-credito'", hasKey("post"))
                .body(
                        "components.schemas.SolicitacaoCredito.required",
                        containsInAnyOrder(
                                "idade",
                                "rendaMensal",
                                "valorSolicitado"
                        )
                )
                .body(
                        "components.schemas.ResultadoAnalise"
                                + ".properties.status.enum",
                        containsInAnyOrder("APROVADO", "REPROVADO")
                );
    }

    @Test
    @DisplayName("Deve retornar HTTP 405 para POST na documentação Swagger")
    void deveRejeitarPostNoSwagger() {
        given()
                .baseUri(enderecoApi)
                .log().ifValidationFails()
                .when()
                .post("/docs")
                .then()
                .log().ifValidationFails()
                .statusCode(405)
                .header("Allow", equalTo("GET"))
                .contentType("application/json")
                .body("erro", equalTo(
                        "Método HTTP não permitido."
                ));
    }

    @Test
    @DisplayName("Deve retornar HTTP 405 para POST na especificação OpenAPI")
    void deveRejeitarPostNaEspecificacao() {
        given()
                .baseUri(enderecoApi)
                .log().ifValidationFails()
                .when()
                .post("/openapi.json")
                .then()
                .log().ifValidationFails()
                .statusCode(405)
                .header("Allow", equalTo("GET"))
                .contentType("application/json")
                .body("erro", equalTo(
                        "Método HTTP não permitido."
                ));
    }

    @Test
    @DisplayName("Deve retornar HTTP 404 para subcaminho inexistente em /docs")
    void deveRejeitarSubcaminhoDoSwagger() {
        given()
                .baseUri(enderecoApi)
                .log().ifValidationFails()
                .when()
                .get("/docs/inexistente")
                .then()
                .log().ifValidationFails()
                .statusCode(404)
                .contentType("application/json")
                .body("erro", equalTo(
                        "Endpoint não encontrado."
                ));
    }

    @Test
    @DisplayName("Deve retornar HTTP 404 para sufixo inválido em /openapi.json")
    void deveRejeitarSufixoDaEspecificacao() {
        given()
                .baseUri(enderecoApi)
                .log().ifValidationFails()
                .when()
                .get("/openapi.json/inexistente")
                .then()
                .log().ifValidationFails()
                .statusCode(404)
                .contentType("application/json")
                .body("erro", equalTo(
                        "Endpoint não encontrado."
                ));
    }
}
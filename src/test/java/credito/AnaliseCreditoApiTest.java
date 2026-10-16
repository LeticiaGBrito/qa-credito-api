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
}
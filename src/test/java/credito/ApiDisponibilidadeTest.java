package credito;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class ApiDisponibilidadeTest {

    private static HttpServer servidor;
    private static String enderecoApi;

    @BeforeAll
    static void iniciarApiLocal() throws IOException {
        servidor = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0
        );

        servidor.createContext("/health", requisicao -> {
            byte[] resposta = """
                    {"status":"UP"}
                    """.getBytes(StandardCharsets.UTF_8);

            requisicao.getResponseHeaders().set(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            requisicao.sendResponseHeaders(200, resposta.length);

            try (var corpoResposta = requisicao.getResponseBody()) {
                corpoResposta.write(resposta);
            }
        });

        servidor.start();

        enderecoApi = "http://127.0.0.1:"
                + servidor.getAddress().getPort();
    }

    @Test
    @DisplayName("Deve retornar status UP quando a API estiver disponível")
    void deveConfirmarDisponibilidadeDaApi() {
        given()
                .baseUri(enderecoApi)
                .when()
                .get("/health")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("status", equalTo("UP"));
    }

    @AfterAll
    static void encerrarApiLocal() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }
}
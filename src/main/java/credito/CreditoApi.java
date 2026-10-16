package credito;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CreditoApi implements AutoCloseable {

    private final HttpServer servidor;

    private final AnaliseCreditoService service =
            new AnaliseCreditoService();

    private final Gson gson = new GsonBuilder()
            .setStrictness(Strictness.STRICT)
            .create();

    /*
     * Interface Swagger UI.
     * A especificação é consultada na própria API.
     * Os arquivos visuais são carregados por CDN.
     */
    private static final String SWAGGER_HTML = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport"
                      content="width=device-width, initial-scale=1.0">

                <title>QA Crédito API — Java</title>

                <link
                    rel="icon"
                    type="image/svg+xml"
                    href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 64 64'%3E%3Crect width='64' height='64' rx='14' fill='%23087f8c'/%3E%3Ctext x='32' y='33' dominant-baseline='middle' text-anchor='middle' font-family='Arial,sans-serif' font-size='30' font-weight='700' fill='white'%3ELG%3C/text%3E%3C/svg%3E"
                >

                <link
                    rel="stylesheet"
                    href="https://unpkg.com/swagger-ui-dist@5.32.0/swagger-ui.css"
                >

                <style>
                    body {
                        margin: 0;
                        background: #fafafa;
                    }

                    .aviso {
                        padding: 14px 20px;
                        background: #e2f2ef;
                        color: #123047;
                        font-family: Arial, sans-serif;
                        font-size: 14px;
                        line-height: 1.6;
                    }

                    .aviso a {
                        color: #123047;
                    }
                </style>
            </head>

            <body>
                <div class="aviso">
                    <strong>Projeto educacional.</strong>
                    Utilize apenas dados fictícios.
                    As regras não representam políticas reais
                    de concessão de crédito.
                    <a href="/openapi.json">Consultar OpenAPI JSON</a>
                </div>

                <div id="swagger-ui"></div>

                <noscript>
                    Ative o JavaScript para utilizar a documentação
                    interativa da API.
                </noscript>

                <script
                    src="https://unpkg.com/swagger-ui-dist@5.32.0/swagger-ui-bundle.js"
                    crossorigin="anonymous"
                ></script>

                <script>
                    window.onload = function () {
                        if (typeof SwaggerUIBundle === "undefined") {
                            document.getElementById("swagger-ui").textContent =
                                "Não foi possível carregar o Swagger UI. "
                                + "Verifique sua conexão com a internet.";
                            return;
                        }

                        window.ui = SwaggerUIBundle({
                            url: "/openapi.json",
                            dom_id: "#swagger-ui",
                            deepLinking: true,
                            displayRequestDuration: true,
                            validatorUrl: null,
                            persistAuthorization: false,
                            presets: [
                                SwaggerUIBundle.presets.apis
                            ],
                            layout: "BaseLayout"
                        });
                    };
                </script>
            </body>
            </html>
            """;

    // Mantém a execução local utilizada pelos testes.
    public CreditoApi(int porta) throws IOException {
        this("127.0.0.1", porta);
    }

    // Permite configurar o endereço de escuta na hospedagem.
    public CreditoApi(String host, int porta) throws IOException {
        servidor = HttpServer.create(
                new InetSocketAddress(host, porta), 0
        );

        servidor.createContext(
                "/health",
                this::verificarDisponibilidade
        );

        servidor.createContext(
                "/analises-credito",
                this::analisarCredito
        );

        servidor.createContext(
                "/docs",
                this::exibirDocumentacao
        );

        servidor.createContext(
                "/openapi.json",
                this::exibirEspecificacao
        );
    }

    public void iniciar() {
        servidor.start();
    }

    // Endereço para consultas locais, incluindo os testes.
    public String endereco() {
        return "http://127.0.0.1:"
                + servidor.getAddress().getPort();
    }

    private boolean validarRota(
            HttpExchange requisicao,
            String caminho,
            String metodo
    ) throws IOException {
        if (!caminho.equals(requisicao.getRequestURI().getPath())) {
            responder(requisicao, 404, Map.of(
                    "erro", "Endpoint não encontrado."
            ));
            return false;
        }

        if (!metodo.equals(requisicao.getRequestMethod())) {
            requisicao.getResponseHeaders().set("Allow", metodo);

            responder(requisicao, 405, Map.of(
                    "erro", "Método HTTP não permitido."
            ));
            return false;
        }

        return true;
    }

    private void verificarDisponibilidade(
            HttpExchange requisicao
    ) throws IOException {
        try {
            if (!validarRota(requisicao, "/health", "GET")) {
                return;
            }

            responder(requisicao, 200, Map.of("status", "UP"));
        } finally {
            requisicao.close();
        }
    }

    /*
     * Exibe a interface Swagger em /docs.
     * Também aceita /docs/ para facilitar o acesso pelo navegador.
     */
    private void exibirDocumentacao(
            HttpExchange requisicao
    ) throws IOException {
        try {
            String caminho = requisicao.getRequestURI().getPath();

            String rotaEsperada = "/docs/".equals(caminho)
                    ? "/docs/"
                    : "/docs";

            if (!validarRota(requisicao, rotaEsperada, "GET")) {
                return;
            }

            responderBytes(
                    requisicao,
                    200,
                    "text/html; charset=UTF-8",
                    SWAGGER_HTML.getBytes(StandardCharsets.UTF_8)
            );
        } finally {
            requisicao.close();
        }
    }

    /*
     * Disponibiliza a especificação OpenAPI.
     * O arquivo é carregado do classpath, funcionando também
     * quando a aplicação é empacotada em JAR e executada no Docker.
     */
    private void exibirEspecificacao(
            HttpExchange requisicao
    ) throws IOException {
        try {
            if (!validarRota(requisicao, "/openapi.json", "GET")) {
                return;
            }

            try (var arquivo = CreditoApi.class.getResourceAsStream(
                    "/openapi.json"
            )) {
                if (arquivo == null) {
                    responder(requisicao, 500, Map.of(
                            "erro",
                            "Especificação OpenAPI não encontrada."
                    ));
                    return;
                }

                responderBytes(
                        requisicao,
                        200,
                        "application/json; charset=UTF-8",
                        arquivo.readAllBytes()
                );
            }
        } finally {
            requisicao.close();
        }
    }

    private void analisarCredito(
            HttpExchange requisicao
    ) throws IOException {
        try {
            if (!validarRota(
                    requisicao,
                    "/analises-credito",
                    "POST"
            )) {
                return;
            }

            String contentType = requisicao.getRequestHeaders()
                    .getFirst("Content-Type");

            if (contentType == null
                    || !contentType.split(";", 2)[0].trim()
                    .equalsIgnoreCase("application/json")) {
                responder(requisicao, 415, Map.of(
                        "erro",
                        "Utilize Content-Type application/json."
                ));
                return;
            }

            String corpo = new String(
                    requisicao.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            JsonElement json = gson.fromJson(
                    corpo,
                    JsonElement.class
            );

            if (json == null || !json.isJsonObject()) {
                throw new IllegalArgumentException(
                        "O corpo da requisição deve ser um objeto JSON."
                );
            }

            JsonObject dados = json.getAsJsonObject();

            BigDecimal idadeInformada = lerNumero(dados, "idade");
            Integer idade = null;

            if (idadeInformada != null) {
                try {
                    idade = idadeInformada.intValueExact();
                } catch (ArithmeticException erro) {
                    throw new IllegalArgumentException(
                            "Idade deve ser um número inteiro entre 0 e 120."
                    );
                }
            }

            BigDecimal renda = lerNumero(dados, "rendaMensal");
            BigDecimal valor = lerNumero(dados, "valorSolicitado");

            var resultado = service.analisar(idade, renda, valor);

            responder(requisicao, 200, resultado);

        } catch (JsonParseException erro) {
            responder(requisicao, 400, Map.of(
                    "erro", "JSON inválido."
            ));
        } catch (IllegalArgumentException erro) {
            responder(requisicao, 400, Map.of(
                    "erro", erro.getMessage()
            ));
        } finally {
            requisicao.close();
        }
    }

    private BigDecimal lerNumero(
            JsonObject dados,
            String campo
    ) {
        JsonElement valor = dados.get(campo);

        if (valor == null || valor.isJsonNull()) {
            return null;
        }

        if (!valor.isJsonPrimitive()
                || !valor.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(
                    "O campo " + campo + " deve ser numérico."
            );
        }

        return valor.getAsBigDecimal();
    }

    // Converte objetos Java em uma resposta JSON.
    private void responder(
            HttpExchange requisicao,
            int statusHttp,
            Object conteudo
    ) throws IOException {
        byte[] resposta = gson.toJson(conteudo)
                .getBytes(StandardCharsets.UTF_8);

        responderBytes(
                requisicao,
                statusHttp,
                "application/json; charset=UTF-8",
                resposta
        );
    }

    // Envia JSON, HTML ou outros conteúdos já convertidos em bytes.
    private void responderBytes(
            HttpExchange requisicao,
            int statusHttp,
            String contentType,
            byte[] resposta
    ) throws IOException {
        requisicao.getResponseHeaders().set(
                "Content-Type",
                contentType
        );

        requisicao.sendResponseHeaders(
                statusHttp,
                resposta.length
        );

        try (var saida = requisicao.getResponseBody()) {
            saida.write(resposta);
        }
    }

    @Override
    public void close() {
        servidor.stop(0);
    }

    public static void main(String[] args) throws IOException {
        String host = System.getenv()
                .getOrDefault("HOST", "127.0.0.1");

        int porta = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080")
        );

        CreditoApi api = new CreditoApi(host, porta);

        Runtime.getRuntime().addShutdownHook(
                new Thread(api::close)
        );

        api.iniciar();

        System.out.println(
                "API iniciada no endereço "
                        + host
                        + ", porta "
                        + porta
        );

        System.out.println(
                "Consulta local de disponibilidade: "
                        + api.endereco()
                        + "/health"
        );

        System.out.println(
                "Documentação Swagger local: "
                        + api.endereco()
                        + "/docs"
        );

        System.out.println(
                "Especificação OpenAPI local: "
                        + api.endereco()
                        + "/openapi.json"
        );
    }
}
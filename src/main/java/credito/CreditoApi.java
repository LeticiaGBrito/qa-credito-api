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

    private void responder(
            HttpExchange requisicao,
            int statusHttp,
            Object conteudo
    ) throws IOException {
        byte[] resposta = gson.toJson(conteudo)
                .getBytes(StandardCharsets.UTF_8);

        requisicao.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
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
    }
}
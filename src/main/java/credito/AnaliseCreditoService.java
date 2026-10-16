package credito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AnaliseCreditoService {

    private static final BigDecimal RENDA_MINIMA =
            new BigDecimal("2000.00");

    private static final BigDecimal MULTIPLICADOR_LIMITE =
            new BigDecimal("5");

    public Resultado analisar(
            Integer idade,
            BigDecimal rendaMensal,
            BigDecimal valorSolicitado
    ) {
        validarDados(idade, rendaMensal, valorSolicitado);

        List<String> motivos = new ArrayList<>();

        if (idade < 18) {
            motivos.add("Solicitante deve ter pelo menos 18 anos.");
        }

        if (rendaMensal.compareTo(RENDA_MINIMA) < 0) {
            motivos.add("Renda mensal inferior a R$ 2.000,00.");
        }

        BigDecimal limitePermitido =
                rendaMensal.multiply(MULTIPLICADOR_LIMITE);

        if (valorSolicitado.compareTo(limitePermitido) > 0) {
            motivos.add(
                    "Valor solicitado superior a cinco vezes a renda mensal."
            );
        }

        String status = motivos.isEmpty()
                ? "APROVADO"
                : "REPROVADO";

        return new Resultado(status, List.copyOf(motivos));
    }

    private void validarDados(
            Integer idade,
            BigDecimal rendaMensal,
            BigDecimal valorSolicitado
    ) {
        if (idade == null || rendaMensal == null
                || valorSolicitado == null) {
            throw new IllegalArgumentException(
                    "Idade, renda mensal e valor solicitado são obrigatórios."
            );
        }

        if (idade < 0 || idade > 120) {
            throw new IllegalArgumentException(
                    "Idade deve estar entre 0 e 120."
            );
        }

        validarValorMonetario(rendaMensal, "Renda mensal");
        validarValorMonetario(valorSolicitado, "Valor solicitado");
    }

    private void validarValorMonetario(
            BigDecimal valor,
            String campo
    ) {
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException(
                    campo + " deve ser maior que zero."
            );
        }

        if (valor.scale() > 2) {
            throw new IllegalArgumentException(
                    campo + " deve ter no máximo duas casas decimais."
            );
        }
    }

    public record Resultado(
            String status,
            List<String> motivos
    ) {
    }
}
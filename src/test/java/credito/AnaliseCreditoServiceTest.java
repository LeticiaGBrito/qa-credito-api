package credito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnaliseCreditoServiceTest {

    private final AnaliseCreditoService service =
            new AnaliseCreditoService();

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'Aprovar solicitação válida', 30, 3000.00, 10000.00",
            "'Aprovar idade mínima', 18, 3000.00, 10000.00",
            "'Aprovar renda mínima', 30, 2000.00, 5000.00",
            "'Aprovar limite exato', 30, 2000.00, 10000.00"
    })
    void deveAprovarSolicitacao(
            String cenario,
            int idade,
            BigDecimal renda,
            BigDecimal valor
    ) {
        var resultado = service.analisar(idade, renda, valor);

        assertAll(
                () -> assertEquals("APROVADO", resultado.status()),
                () -> assertTrue(resultado.motivos().isEmpty())
        );
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'Reprovar menor de idade', 17, 3000.00, 5000.00, 'Solicitante deve ter pelo menos 18 anos.'",
            "'Reprovar renda abaixo do mínimo', 30, 1999.99, 5000.00, 'Renda mensal inferior a R$ 2.000,00.'",
            "'Reprovar valor acima do limite', 30, 2000.00, 10000.01, 'Valor solicitado superior a cinco vezes a renda mensal.'"
    })
    void deveReprovarSolicitacao(
            String cenario,
            int idade,
            BigDecimal renda,
            BigDecimal valor,
            String motivoEsperado
    ) {
        var resultado = service.analisar(idade, renda, valor);

        assertAll(
                () -> assertEquals("REPROVADO", resultado.status()),
                () -> assertEquals(
                        List.of(motivoEsperado),
                        resultado.motivos()
                )
        );
    }

    @Test
    @DisplayName("Deve retornar todos os motivos de reprovação")
    void deveRetornarTodosOsMotivos() {
        var resultado = service.analisar(
                17,
                new BigDecimal("1000.00"),
                new BigDecimal("6000.00")
        );

        assertAll(
                () -> assertEquals("REPROVADO", resultado.status()),
                () -> assertEquals(
                        List.of(
                                "Solicitante deve ter pelo menos 18 anos.",
                                "Renda mensal inferior a R$ 2.000,00.",
                                "Valor solicitado superior a cinco vezes a renda mensal."
                        ),
                        resultado.motivos()
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "'Idade ausente', , 3000.00, 5000.00",
            "'Renda ausente', 30, , 5000.00",
            "'Valor ausente', 30, 3000.00, ",
            "'Idade negativa', -1, 3000.00, 5000.00",
            "'Idade acima do máximo', 121, 3000.00, 5000.00",
            "'Renda zero', 30, 0, 5000.00",
            "'Renda negativa', 30, -0.01, 5000.00",
            "'Valor zero', 30, 3000.00, 0",
            "'Valor negativo', 30, 3000.00, -0.01",
            "'Renda com três casas decimais', 30, 3000.001, 5000.00",
            "'Valor com três casas decimais', 30, 3000.00, 5000.001"
    })
    void deveRejeitarDadosInvalidos(
            String cenario,
            Integer idade,
            BigDecimal renda,
            BigDecimal valor
    ) {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.analisar(idade, renda, valor)
        );
    }
}
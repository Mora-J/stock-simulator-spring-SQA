package ucab.edu.ve.stocksimulator.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// CP-05 (QA - Adecuación Funcional, prioridad alta).
// Transaction.amount es de tipo Float, que arrastra errores de redondeo binario en operaciones
// financieras. Este test debe fallar mientras el atributo siga siendo Float, y debe pasar cuando
// el equipo migre el campo a BigDecimal.
class TransactionAmountRoundingTest {

    @Test
    void floatAmountAccumulatesBinaryRoundingError() {
        Transaction transaction = new Transaction();

        float sum = 10.05f + 5.06f;
        transaction.setAmount(sum);

        assertEquals(15.11f, transaction.getAmount(), 0.0f,
                "BUG: Transaction.amount es Float y arrastra error de redondeo binario; 10.05 + 5.06 "
                        + "no da exactamente 15.11 hasta migrar el atributo a BigDecimal.");
    }
}

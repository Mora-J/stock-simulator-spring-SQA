package ucab.edu.ve.stocksimulator.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

// CP-06 (QA - Adecuación Funcional, prioridad alta).
// OwnedStock es una entidad anemica: setQuantity(int) no valida que la cantidad resultante sea
// negativa. Este test simula un saldo de 50 acciones reducido en 60 y documenta que el resultado
// negativo no es rechazado; debe fallar mientras no exista una salvaguarda de dominio.
class OwnedStockNegativeBalanceTest {

    @Test
    void reducingQuantityBelowZeroShouldBeRejectedByEntity() {
        OwnedStock ownedStock = new OwnedStock();
        ownedStock.setQuantity(50);

        int requestedReduction = 60;
        ownedStock.setQuantity(ownedStock.getQuantity() - requestedReduction);

        assertTrue(ownedStock.getQuantity() >= 0,
                "BUG: OwnedStock.setQuantity no valida que la cantidad resultante sea negativa; "
                        + "reducir 60 acciones de un saldo de 50 deja quantity en " + ownedStock.getQuantity() + ".");
    }
}

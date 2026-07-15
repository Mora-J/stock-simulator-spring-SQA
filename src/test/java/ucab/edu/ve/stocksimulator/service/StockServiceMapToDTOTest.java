package ucab.edu.ve.stocksimulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ucab.edu.ve.stocksimulator.dto.StockDTO;
import ucab.edu.ve.stocksimulator.model.Stock;
import ucab.edu.ve.stocksimulator.repository.StockRepo;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// CP-08 (QA - Adecuación Funcional, prioridad media).
// Verifica, campo por campo via reflexion, que StockService.mapStocktoDTO no pierda ningun
// atributo de la entidad Stock al mapear a StockDTO. Documenta que el campo "id" de Stock no
// tiene equivalente en StockDTO, por lo que ese dato se pierde en el mapeo.
@ExtendWith(MockitoExtension.class)
class StockServiceMapToDTOTest {

    @Mock
    private StockRepo stockRepo;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockService(stockRepo);
    }

    @Test
    void mappingShouldPreserveAllEntityFieldsInDTO() throws Exception {
        Stock stock = new Stock(99L, "AAPL", "Apple Inc.", "Fabricante de dispositivos electronicos");

        StockDTO dto = stockService.mapStocktoDTO(stock);

        for (Field entityField : Stock.class.getDeclaredFields()) {
            entityField.setAccessible(true);
            Object expectedValue = entityField.get(stock);

            Field dtoField = findFieldByName(StockDTO.class, entityField.getName());
            assertNotNull(dtoField,
                    "BUG: StockDTO no tiene un campo '" + entityField.getName()
                            + "' -- el mapeo pierde ese dato de la entidad Stock.");

            dtoField.setAccessible(true);
            assertEquals(expectedValue, dtoField.get(dto),
                    "El campo '" + entityField.getName() + "' no se copio correctamente al DTO.");
        }
    }

    private static Field findFieldByName(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}

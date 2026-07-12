package ucab.edu.ve.stocksimulator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ucab.edu.ve.stocksimulator.dto.StockDTO;
import ucab.edu.ve.stocksimulator.dto.response.StockListResponseDTO;
import ucab.edu.ve.stocksimulator.service.OwnedStockService;
import ucab.edu.ve.stocksimulator.service.StockEODService;
import ucab.edu.ve.stocksimulator.service.StockService;
import ucab.edu.ve.stocksimulator.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

// CP-11 (QA - Adecuación Funcional, prioridad baja).
// Verifica que /api/stock/all empaquete la lista de acciones en el wrapper estandar
// StockListResponseDTO en vez de devolver una lista cruda. La implementacion actual ya hace
// esto correctamente, por lo que se espera que este test PASE (confirma un comportamiento
// correcto ya existente).
@ExtendWith(MockitoExtension.class)
class StockControllerResponseWrapperTest {

    @Mock
    private StockService stockService;
    @Mock
    private StockEODService stockEODService;
    @Mock
    private OwnedStockService ownedStockService;
    @Mock
    private UserService userService;

    private StockController stockController;

    @BeforeEach
    void setUp() {
        stockController = new StockController(stockService, stockEODService, ownedStockService, userService);
    }

    @Test
    void availableStocksShouldBeWrappedInStandardResponseDTO() {
        StockDTO appleStock = new StockDTO("AAPL", "Apple Inc.", "Fabricante de dispositivos electronicos");
        StockListResponseDTO expected = new StockListResponseDTO();
        expected.code = 0;
        expected.stockDTOList = List.of(appleStock);
        when(stockService.findAll()).thenReturn(expected);

        ResponseEntity<StockListResponseDTO> response = stockController.getAvailableStocks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(StockListResponseDTO.class, response.getBody(),
                "La respuesta debe venir empaquetada en StockListResponseDTO y no como una lista cruda.");
        assertEquals(1, response.getBody().stockDTOList.size());
        assertEquals("AAPL", response.getBody().stockDTOList.get(0).ticker);
    }
}

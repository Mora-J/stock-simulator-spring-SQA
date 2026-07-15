package ucab.edu.ve.stocksimulator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ucab.edu.ve.stocksimulator.dto.request.SellRequestDTO;
import ucab.edu.ve.stocksimulator.dto.response.MessageResponseDTO;
import ucab.edu.ve.stocksimulator.model.OwnedStock;
import ucab.edu.ve.stocksimulator.service.OwnedStockService;
import ucab.edu.ve.stocksimulator.service.StockEODService;
import ucab.edu.ve.stocksimulator.service.TransactionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// CP-09 (QA - Adecuación Funcional, prioridad alta).
// TransactionController.sellStock nunca comprueba el saldo disponible antes de delegar en
// OwnedStockService/TransactionService: siempre responde 200 OK y persiste la operacion. Este
// test simula un usuario con 5 acciones vendiendo 10 y documenta que el controlador debe
// responder 400 Bad Request sin invocar los servicios de persistencia; debe fallar mientras esa
// validacion no exista.
@ExtendWith(MockitoExtension.class)
class TransactionControllerInsufficientBalanceTest {

    @Mock
    private TransactionService transactionService;
    @Mock
    private OwnedStockService ownedStockService;
    @Mock
    private StockEODService stockEODService;

    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        transactionController = new TransactionController(transactionService, ownedStockService, stockEODService);
    }

    @Test
    void sellingMoreSharesThanOwnedShouldReturnBadRequestAndNeverPersist() {
        SellRequestDTO sellRequestDTO = new SellRequestDTO();
        sellRequestDTO.username = "trader1";
        sellRequestDTO.ticker = "AAPL";
        sellRequestDTO.name = "Apple Inc.";
        sellRequestDTO.quantity = 10;
        sellRequestDTO.amount = 1000f;

        OwnedStock ownedStock = new OwnedStock();
        ownedStock.setQuantity(5);
        lenient().when(ownedStockService.getOwnedStockByUserAndTicker(any(), eq("AAPL"))).thenReturn(ownedStock);

        ResponseEntity<MessageResponseDTO> response = transactionController.sellStock(sellRequestDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "BUG: TransactionController.sellStock() no valida el saldo disponible antes de "
                        + "procesar la venta; con 10 acciones solicitadas y solo 5 en cartera deberia "
                        + "responder 400.");
        verify(ownedStockService, never()).sellStock(any(SellRequestDTO.class));
        verify(transactionService, never()).registerSell(any(SellRequestDTO.class));
    }
}

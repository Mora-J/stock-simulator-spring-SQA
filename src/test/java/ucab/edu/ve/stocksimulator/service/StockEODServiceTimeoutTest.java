package ucab.edu.ve.stocksimulator.service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

// CP-07 (QA - Adecuación Funcional, prioridad alta).
// StockEODService.getLatestStockEODData crea su propio RestTemplate internamente y atrapa
// cualquier excepción (incluido un timeout) devolviendo null en su lugar. Este test simula un
// timeout mediante mockConstruction y documenta que actualmente NO se propaga un error
// controlado ("Servicio Bursátil No Disponible"); debe fallar mientras el catch siga
// silenciando la excepción con un "return null".
class StockEODServiceTimeoutTest {

    @Test
    void timeoutShouldRaiseControlledExceptionInsteadOfReturningNull() {
        StockEODService service = new StockEODService();

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(Object[].class)))
                        .thenThrow(new ResourceAccessException("Timeout al conectar con Tiingo")))) {

            assertThrows(RuntimeException.class, () -> service.getLatestStockEODData("AAPL"),
                    "BUG: ante un timeout, StockEODService.getLatestStockEODData() captura la "
                            + "excepcion internamente y retorna null en lugar de propagar un error "
                            + "controlado tipo 'Servicio Bursatil No Disponible'.");
        }
    }
}

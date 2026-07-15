package ucab.edu.ve.stocksimulator.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ucab.edu.ve.stocksimulator.controller.TransactionController;
import ucab.edu.ve.stocksimulator.service.OwnedStockService;
import ucab.edu.ve.stocksimulator.service.StockEODService;
import ucab.edu.ve.stocksimulator.service.TransactionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

// CP-12 (QA - Adecuación Funcional, prioridad alta).
// SecurityConfiguration.filterChain() usa anyRequest().permitAll(): no existe ningun mecanismo
// de autenticacion JWT en el proyecto. Este test golpea una ruta transaccional sin token y
// documenta que la peticion NO es rechazada; debe fallar mientras no se agregue un filtro que
// devuelva 401/403 para peticiones sin credenciales validas.
@WebMvcTest(TransactionController.class)
@Import(SecurityConfiguration.class)
class SecurityConfigurationJwtTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;
    @MockBean
    private OwnedStockService ownedStockService;
    @MockBean
    private StockEODService stockEODService;

    @Test
    void transactionalRouteWithoutJwtShouldBeRejected() throws Exception {
        when(transactionService.findAllTransactions(anyString())).thenReturn(List.of());

        int status = mockMvc.perform(get("/api/transaction/all").param("username", "trader1"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertTrue(status == 401 || status == 403,
                "BUG: SecurityConfiguration.filterChain() usa anyRequest().permitAll() -- no existe "
                        + "proteccion JWT, por lo que una ruta transaccional sin token responde " + status
                        + " en vez de 401/403.");
    }
}

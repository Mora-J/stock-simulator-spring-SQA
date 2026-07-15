package ucab.edu.ve.stocksimulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ucab.edu.ve.stocksimulator.repository.TransactionRepo;
import ucab.edu.ve.stocksimulator.repository.UserRepo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

// CP-03 (QA - Adecuación Funcional, prioridad alta).
// TransactionService.verifyVISA(String) llama a cardNumber.matches(...) sin comprobar null,
// por lo que un card number null actualmente lanza NullPointerException (crash 500) en vez de
// retornar false de forma segura. El primer test documenta ese defecto y debe fallar mientras
// el metodo no valide null antes de operar sobre el String.
@ExtendWith(MockitoExtension.class)
class TransactionServiceVerifyVisaTest {

    @Mock
    private TransactionRepo transactionRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private EmailSenderService emailSenderService;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepo, userRepo, emailSenderService);
    }

    @Test
    void verifyVISAWithNullCardShouldReturnFalseInsteadOfThrowing() {
        assertDoesNotThrow(() -> {
            boolean result = transactionService.verifyVISA(null);
            assertFalse(result);
        }, "BUG: verifyVISA(null) lanza NullPointerException (crash 500) en vez de retornar false "
                + "de forma segura.");
    }

    @Test
    void verifyVISAWithEmptyCardShouldReturnFalse() {
        assertFalse(transactionService.verifyVISA(""),
                "Una cadena vacia no cumple el patron VISA y debe rechazarse sin excepcion.");
    }
}

package ucab.edu.ve.stocksimulator.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ucab.edu.ve.stocksimulator.dto.request.UserRequestDTO;
import ucab.edu.ve.stocksimulator.service.ContactFormService;
import ucab.edu.ve.stocksimulator.service.EmailSenderService;
import ucab.edu.ve.stocksimulator.service.TransactionService;
import ucab.edu.ve.stocksimulator.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// CP-10 (QA - Adecuación Funcional, prioridad media).
// UserController.registerUser responde HttpStatus.OK (200) incluso cuando el username ya
// existe, delegando el aviso al campo "code" del body. Este test documenta que deberia
// responder 409 Conflict y debe fallar mientras el controlador no use ese status HTTP.
@ExtendWith(MockitoExtension.class)
class UserControllerDuplicateUsernameTest {

    @Mock
    private UserService userService;
    @Mock
    private EmailSenderService emailSenderService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ContactFormService contactFormService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService, emailSenderService, transactionService, contactFormService);
    }

    @Test
    void registeringExistingUsernameShouldReturnConflict() {
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("existingUser");

        when(userService.userExistsByUsername("existingUser")).thenReturn(true);

        ResponseEntity<Object> response = userController.registerUser(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(),
                "BUG: UserController.registerUser() responde 200 OK con code=1 cuando el username "
                        + "ya existe, en vez de HttpStatus.CONFLICT (409).");
    }
}

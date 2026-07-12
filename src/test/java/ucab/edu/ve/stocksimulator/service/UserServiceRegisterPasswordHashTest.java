package ucab.edu.ve.stocksimulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ucab.edu.ve.stocksimulator.dto.request.UserRequestDTO;
import ucab.edu.ve.stocksimulator.model.User;
import ucab.edu.ve.stocksimulator.repository.UserRepo;
import util.PasswordUtil;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// CP-04 (QA - Adecuación Funcional, prioridad alta).
// Verifica, con un ArgumentCaptor sobre UserRepo, que el password nunca llegue en texto plano
// al repositorio: UserService.mapUserRequestDTOToUser ya delega en PasswordUtil.encodePassword,
// asi que se espera que este test PASE (confirma un comportamiento correcto ya existente).
@ExtendWith(MockitoExtension.class)
class UserServiceRegisterPasswordHashTest {

    private static final String PLAIN_PASSWORD = "SuperSecreta123";

    @Mock
    private UserRepo userRepo;
    @Mock
    private EmailSenderService emailSenderService;
    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepo, emailSenderService);
    }

    @Test
    void passwordShouldBeHashedBeforePersisting() {
        UserRequestDTO request = new UserRequestDTO();
        ReflectionTestUtils.setField(request, "firstName", "Miguel");
        ReflectionTestUtils.setField(request, "lastName", "Pinto");
        request.setUsername("miguelp");
        ReflectionTestUtils.setField(request, "password", PLAIN_PASSWORD);
        ReflectionTestUtils.setField(request, "email", "miguel@example.com");

        User mappedUser = userService.mapUserRequestDTOToUser(request);
        when(userRepo.save(any(User.class))).thenReturn(mappedUser);

        userService.createUser(mappedUser);

        verify(userRepo).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotEquals(PLAIN_PASSWORD, savedUser.getHashedPassword(),
                "El password no debe guardarse en texto plano.");
        assertTrue(PasswordUtil.matches(PLAIN_PASSWORD, savedUser.getHashedPassword()),
                "El hash almacenado debe corresponder al password original via BCrypt.");
    }
}

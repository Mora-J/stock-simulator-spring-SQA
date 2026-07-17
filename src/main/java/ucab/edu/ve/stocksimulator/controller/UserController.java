package ucab.edu.ve.stocksimulator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ucab.edu.ve.stocksimulator.dto.request.ConfirmUserRequestDTO;
import ucab.edu.ve.stocksimulator.dto.request.EditRequestDTO;
import ucab.edu.ve.stocksimulator.dto.request.UserRequestDTO;
import ucab.edu.ve.stocksimulator.dto.response.MessageResponseDTO;
import ucab.edu.ve.stocksimulator.dto.response.UserResponseDTO;
import ucab.edu.ve.stocksimulator.model.User;
import ucab.edu.ve.stocksimulator.service.ContactFormService;
import ucab.edu.ve.stocksimulator.service.EmailSenderService;
import ucab.edu.ve.stocksimulator.service.TransactionService;
import ucab.edu.ve.stocksimulator.service.UserService;
import util.PasswordUtil;
import java.util.List;


@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final EmailSenderService emailSenderService;
    private final TransactionService transactionService;
    private final ContactFormService contactFormService;

    @Autowired
    public UserController(UserService userService, EmailSenderService emailSenderService, TransactionService transactionService, ContactFormService contactFormService) {
        this.userService = userService;
        this.emailSenderService = emailSenderService;
        this.transactionService = transactionService;
        this.contactFormService = contactFormService;
    }

    @PostMapping(value= "/register", produces = "application/json")
    public ResponseEntity<Object> registerUser(@RequestBody UserRequestDTO user) {
        if (userService.userExistsByUsername(user.getUsername())) {
            MessageResponseDTO message = new MessageResponseDTO(1, "User already exists");
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
        else if (userService.userExistsByEmail(user.getEmail())) {
            MessageResponseDTO message = new MessageResponseDTO(2, "Email already exists");
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
        else {
            String code = PasswordUtil.generateRandomCode();
            //this.userService.sendConfirmationEmail(user, code);
            User createdUser = userService.mapUserRequestDTOToUser(user);
            createdUser.setConfirmationCode(code);
            createdUser.setVerified(true);
            createdUser.setConfirmationCode(null);
            userService.createUser(createdUser);
            UserResponseDTO userResponse = userService.mapUserToUserResponseDTO(createdUser);
            return ResponseEntity.status(HttpStatus.OK).body(userResponse);
        }
    }

    @PostMapping(value= "/login", produces = "application/json")
    public ResponseEntity<Object> loginUser(@RequestBody UserRequestDTO user) {
        if (userService.userExistsByUsername(user.getUsername())) {
            User matchedUser = userService.findUserByUsername(user.getUsername());
            if (PasswordUtil.matches(user.getPassword(), matchedUser.getHashedPassword())) {
                UserResponseDTO userResponse = userService.mapUserToUserResponseDTO(matchedUser);
                return ResponseEntity.status(HttpStatus.OK).body(userResponse);
            } else {
                MessageResponseDTO message = new MessageResponseDTO(1, "Incorrect password");
                return ResponseEntity.status(HttpStatus.OK).body(message);
            }
        }
        else {
            MessageResponseDTO message = new MessageResponseDTO(2, "User doesn't exist");
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
    }

    @PostMapping(value = "/confirm", produces = "application/json")
    public ResponseEntity<MessageResponseDTO> confirmUser(@RequestBody ConfirmUserRequestDTO confirmUser) {
        User user = userService.findUserByUsername(confirmUser.getUsername());
        MessageResponseDTO message;
        if (user.getConfirmationCode().equals(confirmUser.getConfirmationCode())) {
            user.setVerified(true);
            user.setConfirmationCode(null);
            userService.updateUser(user);
            message = new MessageResponseDTO(0, "User confirmed Successfully");
        }
        else {
            message = new MessageResponseDTO(1, "Incorrect confirmation code");
        }
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponseDTO> usersResponse = userService.mapUserListToUserResponseDTOList(users);
        return ResponseEntity.status(HttpStatus.OK).body(usersResponse);
    }

    @PostMapping(value = "/edit", produces = "application/json")
    public ResponseEntity<Object> editUser(@RequestBody EditRequestDTO user) {
        User oldUser = userService.findUserByUsername(user.getOldUsername());
        if (user.getUsername() != null && userService.userExistsByUsername(user.getUsername())) {
            MessageResponseDTO message = new MessageResponseDTO(1, "Username already exists");
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
        if (user.getEmail() != null && userService.userExistsByEmail(user.getEmail())) {
            MessageResponseDTO message = new MessageResponseDTO(2, "Email already exists");
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
        if (user.getUsername() != null) {
            oldUser.setUsername(user.getUsername());
        }
        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
            if (!oldUser.getVerified()) {
                String code = PasswordUtil.generateRandomCode();
                oldUser.setConfirmationCode(code);
                //Esto se hace solo para que no llegue null al enviar el correo
                user.setUsername(oldUser.getUsername());
                this.userService.sendConfirmationEmail(user, code);
            }
        }
        if (user.getPassword() != null) {
            oldUser.setHashedPassword(PasswordUtil.encodePassword(user.getPassword()));
        }
        oldUser.setFirstName(user.getFirstName());
        oldUser.setLastName(user.getLastName());
        userService.updateUser(oldUser);
        MessageResponseDTO message = new MessageResponseDTO(0, "User updated successfully");
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    @PostMapping("/delete")
    public ResponseEntity<MessageResponseDTO> removeUser(String username) {
        transactionService.deleteUserInTransactions(username);
        contactFormService.deleteAllUserForms(username);
        userService.removeUser(username);
        MessageResponseDTO message = new MessageResponseDTO(0, "User removed successfully");
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }
}

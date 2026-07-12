package ucab.edu.ve.stocksimulator.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

// CP-01 (QA - Adecuación Funcional, prioridad alta).
// BuyRequestDTO no declara ninguna anotación de Bean Validation (@NotBlank, @Positive, etc.),
// por lo que este test debe fallar mientras el DTO no valide sus campos: documenta que un
// payload con quantity negativa/cero y ticker vacío llegaría intacto al controlador.
class BuyRequestDTOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void negativeQuantityAndEmptyTickerShouldBeRejectedByValidator() {
        BuyRequestDTO dto = new BuyRequestDTO();
        dto.username = "trader1";
        dto.ticker = "";
        dto.name = "Apple Inc.";
        dto.quantity = -10;
        dto.amount = 100f;

        Set<ConstraintViolation<BuyRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty(),
                "BUG: BuyRequestDTO no tiene anotaciones de validacion (@NotBlank/@Positive), por lo que "
                        + "quantity=-10 y ticker vacio no producen ConstraintViolations y el objeto llegaria "
                        + "intacto al controlador.");
    }

    @Test
    void zeroQuantityShouldBeRejectedByValidator() {
        BuyRequestDTO dto = new BuyRequestDTO();
        dto.username = "trader1";
        dto.ticker = "AAPL";
        dto.name = "Apple Inc.";
        dto.quantity = 0;
        dto.amount = 100f;

        Set<ConstraintViolation<BuyRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty(),
                "BUG: quantity=0 no es rechazado porque BuyRequestDTO no declara @Positive/@Min.");
    }
}

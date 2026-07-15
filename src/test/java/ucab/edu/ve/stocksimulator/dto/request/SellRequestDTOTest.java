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

// CP-02 (QA - Adecuación Funcional, prioridad media).
// SellRequestDTO no declara ninguna anotación de Bean Validation, por lo que dejar el ticker
// (identificador de la acción) en null no genera ConstraintViolations. Este test documenta ese
// defecto y debe fallar mientras el DTO no marque el campo como obligatorio (@NotNull/@NotBlank).
class SellRequestDTOTest {

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
    void nullTickerShouldBeRejectedAsMissingRequiredField() {
        SellRequestDTO dto = new SellRequestDTO();
        dto.username = "trader1";
        dto.ticker = null;
        dto.name = "Apple Inc.";
        dto.quantity = 5;
        dto.amount = 100f;

        Set<ConstraintViolation<SellRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty(),
                "BUG: SellRequestDTO no tiene anotaciones de validacion (@NotNull), por lo que un "
                        + "ticker null no produce ConstraintViolations y el objeto llegaria intacto al "
                        + "controlador.");
    }
}

package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// CP-13 (QA - Adecuación Funcional, prioridad media).
// PasswordUtil.matches ya delega correctamente en BCryptPasswordEncoder, por lo que se espera
// que ambos tests PASEN (confirman un comportamiento correcto ya existente).
class PasswordUtilMatchesTest {

    @Test
    void matchesReturnsTrueForCorrectPassword() {
        String rawPassword = "MiClaveSegura123";
        String hashed = PasswordUtil.encodePassword(rawPassword);

        assertTrue(PasswordUtil.matches(rawPassword, hashed));
    }

    @Test
    void matchesReturnsFalseWhenPasswordIsAltered() {
        String rawPassword = "MiClaveSegura123";
        String hashed = PasswordUtil.encodePassword(rawPassword);

        String alteredPassword = "MiClaveSegura124";
        assertFalse(PasswordUtil.matches(alteredPassword, hashed));
    }
}

package util;

import org.junit.jupiter.api.Test;

public class GenerateHashTest {
    @Test
    void printPassword123Hash() {
        String hashed = PasswordUtil.encodePassword("Password123");
        System.out.println(hashed);
    }
}
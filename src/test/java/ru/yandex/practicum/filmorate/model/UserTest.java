package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest {
    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@email.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(new Date());
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Валидация должны быть пройдена - данные заполнены корректно");
    }

    @Test
    public void testInvalidEmail() {
        User user = new User();
        user.setId(1L);
        user.setEmail("invalid-email");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(new Date());
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Не должно пройти валидацию из-за некорректного email");
        violations.forEach(v -> System.out.println(v.getMessage()));
    }

    @Test
    public void testNullEmail() {
        User user = new User();
        user.setId(1L);
        user.setEmail(null);
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(new Date());
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Не должно пройти валидацию из-за email == null");
        violations.forEach(v -> System.out.println(v.getMessage()));
    }

    @Test
    public void testBlankEmail() {
        User user = new User();
        user.setId(1L);
        user.setEmail("");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(new Date());
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Не должно пройти валидацию из-за пустого email");
        violations.forEach(v -> System.out.println(v.getMessage()));
    }
}

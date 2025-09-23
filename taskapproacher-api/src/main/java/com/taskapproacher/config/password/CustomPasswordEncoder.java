package com.taskapproacher.config.password;

import com.taskapproacher.constant.ExceptionMessage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

public class CustomPasswordEncoder implements PasswordEncoder {
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Override
    public String encode(CharSequence rawPassword) {
        validate(rawPassword.toString());
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private void validate(String password) {
        ValidatedPassword validatedPassword = new ValidatedPassword(password);
        Set<ConstraintViolation<ValidatedPassword>> violations = validator.validate(validatedPassword);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(ExceptionMessage.INVALID_PASSWORD_LENGTH.toString(), violations);
        }
    }
}

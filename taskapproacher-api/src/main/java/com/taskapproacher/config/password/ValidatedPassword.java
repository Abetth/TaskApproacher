package com.taskapproacher.config.password;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ValidatedPassword {
    @NotNull
    @Size(min = PasswordConstants.MIN_LENGTH)
    private final String password;

    ValidatedPassword(String password) {
        this.password = password;
    }
}

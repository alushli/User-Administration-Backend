package com.example.userAdministrationApplication.exceptions;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String emailSuffix) {
        super(String.format("Password for users with email suffix \"%s\" must have at least 12 characters", emailSuffix));
    }
}

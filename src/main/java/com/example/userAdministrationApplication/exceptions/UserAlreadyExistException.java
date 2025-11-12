package com.example.userAdministrationApplication.exceptions;

public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String email) {
        super(String.format("User with email \"%s\" already exist", email));
    }
}

package com.example.userAdministrationApplication.exceptions;

public class DbConnectionException extends RuntimeException {
    public DbConnectionException(String message) {
        super(message);
    }
}

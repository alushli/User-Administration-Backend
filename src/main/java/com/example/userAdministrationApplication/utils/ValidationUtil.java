package com.example.userAdministrationApplication.utils;

import com.example.userAdministrationApplication.exceptions.InvalidPasswordException;

public class ValidationUtil {
    private final static String EMAIL_SUFFIX = "example.com";

    public static void validatePasswordForSpecificUsers(String email, String password) {
        if (email.endsWith(EMAIL_SUFFIX) && password.length() < 12) {
            throw new InvalidPasswordException(EMAIL_SUFFIX);
        }
    }
}

package com.helpmeout.common.exception;

import lombok.Getter;

/**
 * Exception thrown when authentication fails for various reasons:
 * - Invalid credentials (wrong password)
 * - User not found
 * - Account inactive
 * - etc.
 */
@Getter
public class AuthenticationFailedException extends RuntimeException {
    private final String errorCode;
    private final String details;

    public AuthenticationFailedException(String message) {
        super(message);
        this.errorCode = "AUTH_FAILED";
        this.details = null;
    }

    public AuthenticationFailedException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public AuthenticationFailedException(String message, String errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

}


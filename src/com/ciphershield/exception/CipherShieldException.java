package com.ciphershield.exception;

/**
 * Base exception for all CipherShield cryptographic and system errors.
 */
public class CipherShieldException extends Exception {
    public CipherShieldException(String message) {
        super(message);
    }
    public CipherShieldException(String message, Throwable cause) {
        super(message, cause);
    }
}

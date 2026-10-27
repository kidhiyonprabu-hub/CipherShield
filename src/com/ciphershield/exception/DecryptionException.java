package com.ciphershield.exception;

public class DecryptionException extends CipherShieldException {
    public DecryptionException(String message) {
        super(message);
    }
    public DecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

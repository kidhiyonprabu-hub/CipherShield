package com.ciphershield.exception;

public class EncryptionException extends CipherShieldException {
    public EncryptionException(String message) {
        super(message);
    }
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

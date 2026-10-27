package com.ciphershield.exception;

public class FileFormatException extends CipherShieldException {
    public FileFormatException(String message) {
        super(message);
    }
    public FileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}

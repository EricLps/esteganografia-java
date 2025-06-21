package com.example.steganography.exceptions;

public class SteganographyException extends Exception {

    public SteganographyException(String message) {
        super(message);
    }

    public SteganographyException(String message, Throwable cause) {
        super(message, cause);
    }
}
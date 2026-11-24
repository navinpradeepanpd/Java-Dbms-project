package com.cinebook.exception;

/**
 * Base checked exception for all business and domain errors in CineBook.
 */
public class CineBookException extends Exception {

    public CineBookException(String message) {
        super(message);
    }

    public CineBookException(String message, Throwable cause) {
        super(message, cause);
    }
}


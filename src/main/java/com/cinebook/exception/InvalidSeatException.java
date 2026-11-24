package com.cinebook.exception;

/**
 * Thrown when a seat code does not conform to cinema layout (e.g. Z9)
 * or does not exist for the selected show.
 */
public class InvalidSeatException extends CineBookException {

    public InvalidSeatException(String message) {
        super(message);
    }
}


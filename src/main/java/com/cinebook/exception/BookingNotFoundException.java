package com.cinebook.exception;

/**
 * Thrown when an entered booking ID cannot be found in the database.
 */
public class BookingNotFoundException extends CineBookException {

    public BookingNotFoundException(String message) {
        super(message);
    }
}


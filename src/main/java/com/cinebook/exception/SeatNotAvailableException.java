package com.cinebook.exception;

/**
 * Thrown when a user attempts to reserve seats that are already booked.
 */
public class SeatNotAvailableException extends CineBookException {

    public SeatNotAvailableException(String message) {
        super(message);
    }
}


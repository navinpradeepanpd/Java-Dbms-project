package com.cinebook.dto;

import com.cinebook.model.Booking;

/**
 * Data Transfer Object for web booking responses.
 */
public class BookingResponse {
    private boolean success;
    private String message;
    private Booking booking;

    public BookingResponse() {}

    public BookingResponse(boolean success, String message, Booking booking) {
        this.success = success;
        this.message = message;
        this.booking = booking;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }
}


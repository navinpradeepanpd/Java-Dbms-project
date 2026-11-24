package com.cinebook.enums;

/**
 * Lifecycle status of a ticket booking.
 */
public enum BookingStatus {
    CONFIRMED,
    CANCELLED;

    public static BookingStatus fromString(String status) {
        if (status == null) return CONFIRMED;
        return valueOf(status.trim().toUpperCase());
    }
}


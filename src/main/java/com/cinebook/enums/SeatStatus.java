package com.cinebook.enums;

/**
 * Availability status of a cinema seat.
 */
public enum SeatStatus {
    AVAILABLE,
    BOOKED;

    public static SeatStatus fromString(String status) {
        if (status == null) return AVAILABLE;
        return valueOf(status.trim().toUpperCase());
    }
}


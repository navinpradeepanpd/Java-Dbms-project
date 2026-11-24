package com.cinebook.enums;

/**
 * Represents the seating category with its associated base pricing.
 * Prices are strictly encapsulated here rather than hardcoded in business logic.
 */
public enum SeatType {
    REGULAR(150.0, "Regular"),
    PREMIUM(250.0, "Premium"),
    VIP(400.0, "VIP");

    private final double price;
    private final String displayName;

    SeatType(double price, String displayName) {
        this.price = price;
        this.displayName = displayName;
    }

    public double getPrice() {
        return price;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Maps row characters ('A'-'E') to their respective SeatType.
     * Rows A-B: Regular (Rs. 150)
     * Rows C-D: Premium (Rs. 250)
     * Row E:    VIP     (Rs. 400)
     */
    public static SeatType fromRow(char rowChar) {
        char upper = Character.toUpperCase(rowChar);
        return switch (upper) {
            case 'A', 'B' -> REGULAR;
            case 'C', 'D' -> PREMIUM;
            case 'E' -> VIP;
            default -> throw new IllegalArgumentException("Invalid row identifier: " + rowChar);
        };
    }
}


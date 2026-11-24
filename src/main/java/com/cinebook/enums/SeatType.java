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
     * Maps row characters ('A'-'L') to their respective SeatType.
     * Rows A-F: Regular (Rs. 150) - 84 seats
     * Rows G-J: Premium (Rs. 250) - 56 seats
     * Rows K-L: VIP     (Rs. 400) - 28 seats (VIP Recliners)
     */
    public static SeatType fromRow(char rowChar) {
        char upper = Character.toUpperCase(rowChar);
        return switch (upper) {
            case 'A', 'B', 'C', 'D', 'E', 'F' -> REGULAR;
            case 'G', 'H', 'I', 'J' -> PREMIUM;
            case 'K', 'L' -> VIP;
            default -> throw new IllegalArgumentException("Invalid row identifier: " + rowChar + ". Valid rows are A through L.");
        };
    }
}


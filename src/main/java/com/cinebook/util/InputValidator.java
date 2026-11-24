package com.cinebook.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Defensive console input utility wrapper around Scanner.
 * Ensures the application never crashes due to malformed user input,
 * invalid numeric formats, empty strings, or mismatched types.
 */
public class InputValidator {

    private final Scanner scanner;

    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Safely reads an integer within the specified inclusive range.
     */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println(">> Input cannot be empty. Please enter a valid number.");
                continue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.printf(">> Please enter a number between %d and %d.%n", min, max);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println(">> Invalid numeric format. Please enter an integer.");
            }
        }
    }

    /**
     * Safely reads a positive integer ID.
     */
    public int readPositiveInt(String prompt) {
        return readInt(prompt, 1, Integer.MAX_VALUE);
    }

    /**
     * Reads a non-empty string.
     */
    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println(">> Input cannot be empty. Please try again.");
        }
    }

    /**
     * Reads an optional string (can be empty).
     */
    public String readOptionalString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Reads a valid phone number (digits only, at least 7 to 15 characters).
     */
    public String readPhoneNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            String phone = scanner.nextLine().trim();
            // Validate digits, spaces, or plus sign
            if (phone.matches("^[0-9]{7,15}$")) {
                return phone;
            }
            System.out.println(">> Invalid phone number. Please enter a valid phone number (7-15 digits, numbers only).");
        }
    }

    /**
     * Reads a list of seat codes separated by commas or spaces.
     * e.g., "A1, A2, B3" or "A1 A2"
     */
    public List<String> readSeatCodes(String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            if (raw.isEmpty()) {
                System.out.println(">> Seat list cannot be empty. Please enter at least one seat.");
                continue;
            }

            // Split by comma or whitespace
            String[] tokens = raw.split("[,\\s]+");
            List<String> codes = Arrays.stream(tokens)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

            if (codes.isEmpty()) {
                System.out.println(">> No valid seat codes recognized. Please try again.");
                continue;
            }
            return codes;
        }
    }

    /**
     * Reads a confirmation prompt (Y/N).
     */
    public boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (Y/N): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if ("Y".equals(input) || "YES".equals(input)) {
                return true;
            }
            if ("N".equals(input) || "NO".equals(input)) {
                return false;
            }
            System.out.println(">> Please enter 'Y' for Yes or 'N' for No.");
        }
    }
}


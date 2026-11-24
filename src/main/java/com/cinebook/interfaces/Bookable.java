package com.cinebook.interfaces;

import com.cinebook.exception.CineBookException;
import com.cinebook.model.Booking;
import com.cinebook.model.Customer;

import java.util.List;

/**
 * Core business contract for cinema ticketing operations.
 * Demonstrates abstraction and interface design in Object-Oriented Programming.
 */
public interface Bookable {

    /**
     * Reserves the requested seats for a customer in a single atomic database transaction.
     *
     * @param customer   customer details
     * @param showId     target show identifier
     * @param seatCodes  list of seat codes (e.g., ["A1", "A2"])
     * @param couponCode optional discount coupon code (e.g., "CINE10"), or null/empty
     * @return confirmed Booking entity with pricing and seat details
     * @throws CineBookException if validation fails or seats are unavailable
     */
    Booking bookSeats(Customer customer, int showId, List<String> seatCodes, String couponCode) throws CineBookException;

    /**
     * Cancels an existing booking atomically: releases seats and issues an 80% refund record.
     *
     * @param bookingId identifier of the booking to cancel
     * @return true if successfully cancelled
     * @throws CineBookException if booking does not exist or was previously cancelled
     */
    boolean cancelBooking(int bookingId) throws CineBookException;
}


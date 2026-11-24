package com.cinebook.exception;

/**
 * Thrown when an invalid or expired discount coupon code is entered.
 */
public class InvalidCouponException extends CineBookException {

    public InvalidCouponException(String message) {
        super(message);
    }
}


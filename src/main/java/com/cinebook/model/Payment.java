package com.cinebook.model;

import java.time.LocalDateTime;

/**
 * Represents an audit record of a monetary transaction (booking payment or refund).
 */
public class Payment {
    private int paymentId;
    private int bookingId;
    private double amount;
    private String paymentType;     // 'BOOKING_PAYMENT' or 'CANCELLATION_REFUND'
    private String paymentStatus;   // 'SUCCESS', 'FAILED'
    private LocalDateTime paymentTime;

    public Payment() {
        this.paymentTime = LocalDateTime.now();
        this.paymentStatus = "SUCCESS";
    }

    public Payment(int paymentId, int bookingId, double amount, String paymentType, String paymentStatus, LocalDateTime paymentTime) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentType = paymentType;
        this.paymentStatus = paymentStatus;
        this.paymentTime = paymentTime;
    }

    public Payment(int bookingId, double amount, String paymentType, String paymentStatus) {
        this(0, bookingId, amount, paymentType, paymentStatus, LocalDateTime.now());
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    @Override
    public String toString() {
        return String.format("[Payment #%d] Booking #%d | Type: %s | Amount: Rs.%.2f | Status: %s",
                paymentId, bookingId, paymentType, amount, paymentStatus);
    }
}


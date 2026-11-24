package com.cinebook.dto;

/**
 * Data Transfer Object for web cancellation responses.
 */
public class CancellationResponse {
    private boolean success;
    private String message;
    private int bookingId;
    private String status;
    private double refundAmount;

    public CancellationResponse() {}

    public CancellationResponse(boolean success, String message, int bookingId, String status, double refundAmount) {
        this.success = success;
        this.message = message;
        this.bookingId = bookingId;
        this.status = status;
        this.refundAmount = refundAmount;
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

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }
}


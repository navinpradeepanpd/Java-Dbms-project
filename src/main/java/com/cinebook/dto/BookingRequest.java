package com.cinebook.dto;

import java.util.List;

/**
 * Data Transfer Object for incoming web booking requests.
 */
public class BookingRequest {
    private String customerName;
    private String phone;
    private int showId;
    private List<String> seatCodes;
    private String couponCode;

    public BookingRequest() {}

    public BookingRequest(String customerName, String phone, int showId, List<String> seatCodes, String couponCode) {
        this.customerName = customerName;
        this.phone = phone;
        this.showId = showId;
        this.seatCodes = seatCodes;
        this.couponCode = couponCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public List<String> getSeatCodes() {
        return seatCodes;
    }

    public void setSeatCodes(List<String> seatCodes) {
        this.seatCodes = seatCodes;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}


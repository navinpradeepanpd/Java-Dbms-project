package com.cinebook.model;

import com.cinebook.enums.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ticket booking transaction.
 */
public class Booking {
    private int bookingId;
    private int customerId;
    private int showId;
    private LocalDateTime bookingTime;
    private double subtotal;
    private double discountAmount;
    private double totalAmount;
    private String couponCode;
    private BookingStatus status;

    // Supplementary presentation attributes
    private String customerName;
    private String customerPhone;
    private int movieId;
    private String movieTitle;
    private LocalDate showDate;
    private LocalTime showTime;
    private String screenName;
    private List<String> bookedSeats = new ArrayList<>();

    public Booking() {
        this.status = BookingStatus.CONFIRMED;
        this.bookingTime = LocalDateTime.now();
    }

    public Booking(int bookingId, int customerId, int showId, LocalDateTime bookingTime,
                   double subtotal, double discountAmount, double totalAmount,
                   String couponCode, BookingStatus status) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.showId = showId;
        this.bookingTime = bookingTime;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.couponCode = couponCode;
        this.status = status;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public LocalTime getShowTime() {
        return showTime;
    }

    public void setShowTime(LocalTime showTime) {
        this.showTime = showTime;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public List<String> getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(List<String> bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    @Override
    public String toString() {
        return String.format("[Booking #%d] %s | Seats: %s | Total: Rs.%.2f | Status: %s",
                bookingId, movieTitle != null ? movieTitle : ("Show #" + showId),
                String.join(", ", bookedSeats), totalAmount, status);
    }
}


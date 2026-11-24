package com.cinebook.model;

import com.cinebook.enums.SeatStatus;
import com.cinebook.enums.SeatType;

/**
 * Represents a single seat instance belonging to a specific show.
 */
public class Seat {
    private int seatId;
    private int showId;
    private String seatCode;
    private SeatType seatType;
    private SeatStatus status;

    public Seat() {}

    public Seat(int seatId, int showId, String seatCode, SeatType seatType, SeatStatus status) {
        this.seatId = seatId;
        this.showId = showId;
        this.seatCode = seatCode;
        this.seatType = seatType;
        this.status = status;
    }

    public Seat(int showId, String seatCode, SeatType seatType, SeatStatus status) {
        this(0, showId, seatCode, seatType, status);
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return String.format("%s (%s - Rs.%.0f) [%s]", seatCode, seatType.getDisplayName(), seatType.getPrice(), status);
    }
}


package com.cinebook.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the cinema theatre / multiplex complex.
 * Holds conceptual responsibility for the venue, screens, scheduled shows,
 * and associated bookings.
 */
public class Theatre {
    private int theatreId;
    private String theatreName;
    private String city;
    private List<Show> shows = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();

    public Theatre() {
        this.theatreName = "CineBook Cinemas";
        this.city = "Downtown";
    }

    public Theatre(int theatreId, String theatreName, String city) {
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.city = city;
    }

    public Theatre(int theatreId, String theatreName, String city, List<Show> shows) {
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.city = city;
        if (shows != null) {
            this.shows = new ArrayList<>(shows);
        }
    }

    public int getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(int theatreId) {
        this.theatreId = theatreId;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<Show> getShows() {
        return shows;
    }

    public void setShows(List<Show> shows) {
        this.shows = (shows != null) ? shows : new ArrayList<>();
    }

    public void addShow(Show show) {
        if (show != null) {
            this.shows.add(show);
        }
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = (bookings != null) ? bookings : new ArrayList<>();
    }

    public void addBooking(Booking booking) {
        if (booking != null) {
            this.bookings.add(booking);
        }
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %d active show(s)", theatreId, theatreName, city, shows.size());
    }
}


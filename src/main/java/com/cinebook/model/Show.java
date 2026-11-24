package com.cinebook.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a scheduled movie screening.
 */
public class Show {
    private int showId;
    private int movieId;
    private LocalDate showDate;
    private LocalTime showTime;
    private String screenName;

    // Associated movie metadata for display
    private String movieName;
    private String language;

    public Show() {}

    public Show(int showId, int movieId, LocalDate showDate, LocalTime showTime, String screenName) {
        this.showId = showId;
        this.movieId = movieId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.screenName = screenName;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
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

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        String movieStr = (movieName != null) ? movieName : ("Movie #" + movieId);
        return String.format("[Show #%d] %s | Date: %s | Time: %s | %s",
                showId, movieStr, showDate, showTime, screenName != null ? screenName : "Screen 1");
    }
}


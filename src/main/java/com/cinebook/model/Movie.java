package com.cinebook.model;

/**
 * Represents a Movie screened at CineBook.
 */
public class Movie {
    private int movieId;
    private String movieName;
    private int duration;       // Duration in minutes
    private String language;
    private String genre;

    public Movie() {}

    public Movie(int movieId, String movieName, int duration, String language, String genre) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.duration = duration;
        this.language = language;
        this.genre = genre;
    }

    public Movie(String movieName, int duration, String language, String genre) {
        this(0, movieName, duration, language, genre);
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s, %s) - %d mins", movieId, movieName, language, genre, duration);
    }
}


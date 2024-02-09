package edu.uci.ics.fabflixmobile.data.model;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String name;
    private final String year;
    private final String director;
    private final String genres;
    private final String stars;
    private final String id;

    public Movie(String name, String year, String director, String genres, String stars, String id) {
        this.name = name;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.stars = stars;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }
    public String getDirector() {
        return director;
    }
    public String getGenres() {
        return genres;
    }
    public String getStars() {
        return stars;
    }
    public String getId() {
        return id;
    }
}
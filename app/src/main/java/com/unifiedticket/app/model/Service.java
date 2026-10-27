package com.unifiedticket.app.model;

/** A bookable item: a bus trip, train trip, movie show or event. */
public class Service {
    public static final String BUS = "BUS", TRAIN = "TRAIN", MOVIE = "MOVIE", EVENT = "EVENT";

    public final long id;
    public final String category, title, source, destination, date, time, operator;
    public final double price;
    public final int totalSeats;

    public Service(long id, String category, String title, String source, String destination,
                   String date, String time, double price, int totalSeats, String operator) {
        this.id = id; this.category = category; this.title = title; this.source = source;
        this.destination = destination; this.date = date; this.time = time;
        this.price = price; this.totalSeats = totalSeats; this.operator = operator;
    }

    /** "Mumbai → Pune" for transport, or the venue name for movies/events. */
    public String route() {
        return (destination == null || destination.isEmpty()) ? source : source + " → " + destination;
    }

    public static String icon(String category) {
        switch (category) {
            case BUS: return "🚌";
            case TRAIN: return "🚆";
            case MOVIE: return "🎬";
            default: return "🎤";
        }
    }

    public static String label(String category) {
        switch (category) {
            case BUS: return "Bus";
            case TRAIN: return "Train";
            case MOVIE: return "Movie";
            default: return "Event";
        }
    }
}

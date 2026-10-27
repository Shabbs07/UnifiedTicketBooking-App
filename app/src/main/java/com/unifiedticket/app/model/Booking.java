package com.unifiedticket.app.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Booking {
    public static final String CONFIRMED = "CONFIRMED", CANCELLED = "CANCELLED";

    public long id, userId, serviceId;
    public String ref, seats, paymentMode, status, bookedAt;
    public double amount;
    // Joined from the services table
    public String category, title, source, destination, travelDate, travelTime;

    public String route() {
        return (destination == null || destination.isEmpty()) ? source : source + " → " + destination;
    }

    /** True once the trip / show start time has passed. */
    public boolean hasDeparted() {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(travelDate + " " + travelTime);
            return d != null && d.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    public boolean isCancellable() {
        return CONFIRMED.equals(status) && !hasDeparted();
    }
}

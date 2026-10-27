package com.unifiedticket.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.unifiedticket.app.model.Booking;
import com.unifiedticket.app.model.Service;
import com.unifiedticket.app.model.User;
import com.unifiedticket.app.util.SecurityUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Single data-access layer. Every query is parameterised (no string concatenation of user input),
 * which prevents SQL injection.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "unified_ticket.db";
    private static final int DB_VERSION = 1;
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper get(Context ctx) {
        if (instance == null) instance = new DatabaseHelper(ctx.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context ctx) { super(ctx, DB_NAME, null, DB_VERSION); }

    @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, phone TEXT, " +
                "password_hash TEXT NOT NULL, salt TEXT NOT NULL)");
        db.execSQL("CREATE TABLE services(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT NOT NULL, title TEXT NOT NULL, " +
                "source TEXT, destination TEXT, travel_date TEXT NOT NULL, travel_time TEXT NOT NULL, " +
                "price REAL NOT NULL, total_seats INTEGER NOT NULL, operator TEXT)");
        db.execSQL("CREATE TABLE bookings(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, booking_ref TEXT NOT NULL UNIQUE, " +
                "user_id INTEGER NOT NULL, service_id INTEGER NOT NULL, seats TEXT NOT NULL, " +
                "amount REAL NOT NULL, payment_mode TEXT, status TEXT NOT NULL, booked_at TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(service_id) REFERENCES services(id))");
        seed(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS bookings");
        db.execSQL("DROP TABLE IF EXISTS services");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // ------------------------------------------------------------------ seed data
    private void seed(SQLiteDatabase db) {
        // category, title, source/venue, destination, dayOffset, time, price, seats, operator
        String[][] rows = {
            {"BUS", "Mumbai → Pune Express", "Mumbai", "Pune", "1", "06:30", "450", "40", "MSRTC Shivneri"},
            {"BUS", "Mumbai → Goa Sleeper", "Mumbai", "Goa", "1", "20:00", "1200", "40", "Neeta Travels"},
            {"BUS", "Mumbai → Nashik AC", "Mumbai", "Nashik", "2", "07:15", "380", "40", "Prasanna Purple"},
            {"BUS", "Pune → Bengaluru Volvo", "Pune", "Bengaluru", "2", "18:45", "1450", "40", "VRL Travels"},
            {"TRAIN", "CSMT → Pune Intercity", "Mumbai", "Pune", "1", "07:10", "290", "40", "Indian Railways"},
            {"TRAIN", "Mumbai Central → Ahmedabad Shatabdi", "Mumbai", "Ahmedabad", "3", "06:25", "1150", "40", "Indian Railways"},
            {"TRAIN", "LTT → Delhi Express", "Mumbai", "Delhi", "2", "16:35", "1850", "40", "Indian Railways"},
            {"TRAIN", "Pune → Hyderabad Express", "Pune", "Hyderabad", "4", "17:20", "720", "40", "Indian Railways"},
            {"MOVIE", "Space Odyssey 2 (IMAX)", "PVR Phoenix, Lower Parel", "", "1", "19:30", "550", "30", "PVR Cinemas"},
            {"MOVIE", "Monsoon Memories (Hindi)", "INOX R-City, Ghatkopar", "", "1", "21:00", "320", "30", "INOX"},
            {"MOVIE", "The Last Heist (English)", "Cinepolis, Andheri", "", "2", "18:00", "380", "30", "Cinepolis"},
            {"EVENT", "Indie Music Night", "Jio World Garden, BKC", "", "5", "19:00", "1500", "40", "Live Nation"},
            {"EVENT", "Stand-up Comedy Special", "NCPA, Nariman Point", "", "6", "20:00", "999", "40", "Comedy Hub"},
            {"EVENT", "Tech Fest 2026", "NESCO, Goregaon", "", "8", "10:00", "499", "40", "TechIndia"},
        };
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        for (String[] r : rows) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, Integer.parseInt(r[4]));
            ContentValues v = new ContentValues();
            v.put("category", r[0]); v.put("title", r[1]); v.put("source", r[2]); v.put("destination", r[3]);
            v.put("travel_date", f.format(c.getTime())); v.put("travel_time", r[5]);
            v.put("price", Double.parseDouble(r[6])); v.put("total_seats", Integer.parseInt(r[7]));
            v.put("operator", r[8]);
            db.insert("services", null, v);
        }
    }

    // ------------------------------------------------------------------ users
    /** @return false if the email is already registered. */
    public boolean registerUser(String name, String email, String phone, String password) {
        String salt = SecurityUtil.generateSalt();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("email", email.toLowerCase(Locale.ROOT));
        v.put("phone", phone);
        v.put("password_hash", SecurityUtil.hash(password, salt));
        v.put("salt", salt);
        return getWritableDatabase().insert("users", null, v) != -1;
    }

    /** @return the user, or null if credentials are wrong. */
    public User authenticate(String email, String password) {
        try (Cursor c = getReadableDatabase().query("users", null, "email=?",
                new String[]{email.toLowerCase(Locale.ROOT)}, null, null, null)) {
            if (c.moveToFirst()) {
                String salt = c.getString(c.getColumnIndexOrThrow("salt"));
                String hash = c.getString(c.getColumnIndexOrThrow("password_hash"));
                if (SecurityUtil.verify(password, salt, hash)) {
                    return new User(c.getLong(c.getColumnIndexOrThrow("id")),
                            c.getString(c.getColumnIndexOrThrow("name")),
                            c.getString(c.getColumnIndexOrThrow("email")),
                            c.getString(c.getColumnIndexOrThrow("phone")));
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ services
    public List<Service> getServices(String category, String query) {
        List<Service> list = new ArrayList<>();
        String where = "category=?";
        List<String> args = new ArrayList<>();
        args.add(category);
        if (query != null && !query.trim().isEmpty()) {
            where += " AND (title LIKE ? OR source LIKE ? OR destination LIKE ?)";
            String q = "%" + query.trim() + "%";
            args.add(q); args.add(q); args.add(q);
        }
        try (Cursor c = getReadableDatabase().query("services", null, where,
                args.toArray(new String[0]), null, null, "travel_date, travel_time")) {
            while (c.moveToNext()) list.add(mapService(c));
        }
        return list;
    }

    public Service getService(long id) {
        try (Cursor c = getReadableDatabase().query("services", null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? mapService(c) : null;
        }
    }

    private Service mapService(Cursor c) {
        return new Service(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("category")),
                c.getString(c.getColumnIndexOrThrow("title")),
                c.getString(c.getColumnIndexOrThrow("source")),
                c.getString(c.getColumnIndexOrThrow("destination")),
                c.getString(c.getColumnIndexOrThrow("travel_date")),
                c.getString(c.getColumnIndexOrThrow("travel_time")),
                c.getDouble(c.getColumnIndexOrThrow("price")),
                c.getInt(c.getColumnIndexOrThrow("total_seats")),
                c.getString(c.getColumnIndexOrThrow("operator")));
    }

    // ------------------------------------------------------------------ bookings
    public Set<Integer> getBookedSeats(long serviceId) {
        Set<Integer> taken = new HashSet<>();
        try (Cursor c = getReadableDatabase().query("bookings", new String[]{"seats"},
                "service_id=? AND status=?", new String[]{String.valueOf(serviceId), Booking.CONFIRMED},
                null, null, null)) {
            while (c.moveToNext()) {
                for (String s : c.getString(0).split(",")) {
                    try { taken.add(Integer.parseInt(s.trim())); } catch (NumberFormatException ignored) { }
                }
            }
        }
        return taken;
    }

    /**
     * Books seats atomically. Availability is re-checked inside the transaction so two users can
     * never hold the same seat.
     *
     * @return the new booking id, or -1 if a seat is invalid / already taken.
     */
    public long createBooking(long userId, long serviceId, List<Integer> seats, String paymentMode) {
        if (seats == null || seats.isEmpty()) return -1;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Service s = getService(serviceId);
            if (s == null) return -1;
            Set<Integer> taken = getBookedSeats(serviceId);
            for (int seat : seats) {
                if (seat < 1 || seat > s.totalSeats || taken.contains(seat)) return -1;
            }
            List<Integer> sorted = new ArrayList<>(new HashSet<>(seats));
            Collections.sort(sorted);
            StringBuilder csv = new StringBuilder();
            for (int i = 0; i < sorted.size(); i++) csv.append(i > 0 ? "," : "").append(sorted.get(i));

            ContentValues v = new ContentValues();
            v.put("booking_ref", SecurityUtil.newBookingRef());
            v.put("user_id", userId);
            v.put("service_id", serviceId);
            v.put("seats", csv.toString());
            v.put("amount", s.price * sorted.size());
            v.put("payment_mode", paymentMode);
            v.put("status", Booking.CONFIRMED);
            v.put("booked_at", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()));
            long id = db.insert("bookings", null, v);
            if (id != -1) db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    private static final String BOOKING_SELECT =
            "SELECT b.id, b.booking_ref, b.user_id, b.service_id, b.seats, b.amount, b.payment_mode, " +
            "b.status, b.booked_at, s.category, s.title, s.source, s.destination, s.travel_date, s.travel_time " +
            "FROM bookings b JOIN services s ON s.id = b.service_id ";

    public List<Booking> getBookings(long userId) {
        List<Booking> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                BOOKING_SELECT + "WHERE b.user_id=? ORDER BY b.id DESC", new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) list.add(mapBooking(c));
        }
        return list;
    }

    /** Scoped by user id, so one user can never open another user's ticket. */
    public Booking getBooking(long bookingId, long userId) {
        try (Cursor c = getReadableDatabase().rawQuery(BOOKING_SELECT + "WHERE b.id=? AND b.user_id=?",
                new String[]{String.valueOf(bookingId), String.valueOf(userId)})) {
            return c.moveToFirst() ? mapBooking(c) : null;
        }
    }

    public boolean cancelBooking(long bookingId, long userId) {
        Booking b = getBooking(bookingId, userId);
        if (b == null || !b.isCancellable()) return false;
        ContentValues v = new ContentValues();
        v.put("status", Booking.CANCELLED);
        return getWritableDatabase().update("bookings", v, "id=? AND user_id=? AND status=?",
                new String[]{String.valueOf(bookingId), String.valueOf(userId), Booking.CONFIRMED}) > 0;
    }

    private Booking mapBooking(Cursor c) {
        Booking b = new Booking();
        b.id = c.getLong(0); b.ref = c.getString(1); b.userId = c.getLong(2); b.serviceId = c.getLong(3);
        b.seats = c.getString(4); b.amount = c.getDouble(5); b.paymentMode = c.getString(6);
        b.status = c.getString(7); b.bookedAt = c.getString(8); b.category = c.getString(9);
        b.title = c.getString(10); b.source = c.getString(11); b.destination = c.getString(12);
        b.travelDate = c.getString(13); b.travelTime = c.getString(14);
        return b;
    }
}

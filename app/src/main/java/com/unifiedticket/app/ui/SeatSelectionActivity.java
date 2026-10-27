package com.unifiedticket.app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.unifiedticket.app.R;
import com.unifiedticket.app.db.DatabaseHelper;
import com.unifiedticket.app.model.Service;
import com.unifiedticket.app.util.Formats;
import com.unifiedticket.app.util.SessionManager;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class SeatSelectionActivity extends AppCompatActivity {
    public static final String EXTRA_SERVICE_ID = "service_id";
    private static final int MAX_SEATS = 6;

    private DatabaseHelper db;
    private SessionManager session;
    private Service service;
    private GridLayout seatGrid;
    private TextView tvSummary;
    private final Set<Integer> selected = new TreeSet<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);
        setTitle("Select seats");

        db = DatabaseHelper.get(this);
        session = new SessionManager(this);
        service = db.getService(getIntent().getLongExtra(EXTRA_SERVICE_ID, -1));
        if (service == null || !session.isLoggedIn()) { finish(); return; }

        ((TextView) findViewById(R.id.tvTitle)).setText(Service.icon(service.category) + "  " + service.title);
        ((TextView) findViewById(R.id.tvInfo)).setText(service.route() + "\n" + service.date + " • " + service.time
                + "\n" + service.operator + " • " + Formats.money(service.price) + " per seat");
        seatGrid = findViewById(R.id.seatGrid);
        tvSummary = findViewById(R.id.tvSummary);
        findViewById(R.id.btnBook).setOnClickListener(v -> askPayment());
        buildSeats();
    }

    private void buildSeats() {
        seatGrid.removeAllViews();
        selected.clear();
        Set<Integer> booked = db.getBookedSeats(service.id);
        int m = dp(4);

        for (int i = 1; i <= service.totalSeats; i++) {
            final int seat = i;
            TextView tv = new TextView(this);
            tv.setText(String.valueOf(seat));
            tv.setGravity(Gravity.CENTER);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            lp.height = dp(44);
            lp.setMargins(m, m, m, m);
            tv.setLayoutParams(lp);

            if (booked.contains(seat)) {
                tv.setBackgroundResource(R.drawable.seat_booked);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackgroundResource(R.drawable.seat_available);
                tv.setTextColor(Color.BLACK);
                tv.setOnClickListener(v -> toggle(seat, tv));
            }
            seatGrid.addView(tv);
        }
        updateSummary();
    }

    private void toggle(int seat, TextView tv) {
        if (selected.contains(seat)) {
            selected.remove(seat);
            tv.setBackgroundResource(R.drawable.seat_available);
            tv.setTextColor(Color.BLACK);
        } else {
            if (selected.size() >= MAX_SEATS) {
                Toast.makeText(this, "You can book up to " + MAX_SEATS + " seats at a time", Toast.LENGTH_SHORT).show();
                return;
            }
            selected.add(seat);
            tv.setBackgroundResource(R.drawable.seat_selected);
            tv.setTextColor(Color.WHITE);
        }
        updateSummary();
    }

    private void updateSummary() {
        if (selected.isEmpty()) {
            tvSummary.setText("No seats selected");
        } else {
            tvSummary.setText("Seats: " + selected + "\nTotal: " + Formats.money(service.price * selected.size()));
        }
    }

    /** Payment is simulated for this project; a real app would call a payment gateway SDK here. */
    private void askPayment() {
        if (selected.isEmpty()) {
            Toast.makeText(this, "Select at least one seat", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] modes = {"UPI", "Credit / Debit card", "Net banking", "Pay at counter"};
        new AlertDialog.Builder(this)
                .setTitle("Pay " + Formats.money(service.price * selected.size()) + " (demo)")
                .setItems(modes, (d, which) -> confirmBooking(modes[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmBooking(String mode) {
        long id = db.createBooking(session.getUserId(), service.id, new ArrayList<>(selected), mode);
        if (id < 0) {
            Toast.makeText(this, "Some seats were just taken. Please choose again.", Toast.LENGTH_LONG).show();
            buildSeats();
            return;
        }
        Intent i = new Intent(this, TicketActivity.class);
        i.putExtra(TicketActivity.EXTRA_BOOKING_ID, id);
        startActivity(i);
        finish();
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}

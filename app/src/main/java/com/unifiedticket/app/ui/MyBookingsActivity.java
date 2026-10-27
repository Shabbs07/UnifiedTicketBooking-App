package com.unifiedticket.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unifiedticket.app.R;
import com.unifiedticket.app.adapter.BookingAdapter;
import com.unifiedticket.app.db.DatabaseHelper;
import com.unifiedticket.app.model.Booking;
import com.unifiedticket.app.util.SessionManager;

import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private SessionManager session;
    private BookingAdapter adapter;
    private TextView tvEmpty;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);
        setTitle("My bookings");
        db = DatabaseHelper.get(this);
        session = new SessionManager(this);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(b -> {
            Intent i = new Intent(this, TicketActivity.class);
            i.putExtra(TicketActivity.EXTRA_BOOKING_ID, b.id);
            startActivity(i);
        });
        rv.setAdapter(adapter);
    }

    @Override protected void onResume() {
        super.onResume();
        List<Booking> list = db.getBookings(session.getUserId());
        adapter.setItems(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}

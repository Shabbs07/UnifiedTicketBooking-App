package com.unifiedticket.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.unifiedticket.app.R;
import com.unifiedticket.app.model.Service;
import com.unifiedticket.app.util.SessionManager;

public class HomeActivity extends AppCompatActivity {
    private SessionManager session;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!session.isLoggedIn()) { logoutToLogin(); return; }

        setContentView(R.layout.activity_home);
        setTitle("Unified Ticket Booking");
        ((TextView) findViewById(R.id.tvGreeting)).setText("Hello, " + session.getName() + " 👋");

        findViewById(R.id.cardBus).setOnClickListener(v -> openCategory(Service.BUS));
        findViewById(R.id.cardTrain).setOnClickListener(v -> openCategory(Service.TRAIN));
        findViewById(R.id.cardMovie).setOnClickListener(v -> openCategory(Service.MOVIE));
        findViewById(R.id.cardEvent).setOnClickListener(v -> openCategory(Service.EVENT));
        findViewById(R.id.btnMyBookings).setOnClickListener(v ->
                startActivity(new Intent(this, MyBookingsActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.logout();
            logoutToLogin();
        });
    }

    private void openCategory(String category) {
        Intent i = new Intent(this, ServiceListActivity.class);
        i.putExtra(ServiceListActivity.EXTRA_CATEGORY, category);
        startActivity(i);
    }

    private void logoutToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}

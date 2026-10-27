package com.unifiedticket.app.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.unifiedticket.app.R;
import com.unifiedticket.app.db.DatabaseHelper;
import com.unifiedticket.app.model.Booking;
import com.unifiedticket.app.model.Service;
import com.unifiedticket.app.util.Formats;
import com.unifiedticket.app.util.SessionManager;

public class TicketActivity extends AppCompatActivity {
    public static final String EXTRA_BOOKING_ID = "booking_id";

    private DatabaseHelper db;
    private SessionManager session;
    private long bookingId;
    private Button btnCancel;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);
        setTitle("E-Ticket");
        db = DatabaseHelper.get(this);
        session = new SessionManager(this);
        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1);
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Cancel this booking?")
                .setMessage("The seats will be released. Refund is processed to the original payment mode (demo).")
                .setPositiveButton("Yes, cancel", (d, w) -> cancel())
                .setNegativeButton("Keep ticket", null)
                .show());
        load();
    }

    private void load() {
        Booking b = session.isLoggedIn() ? db.getBooking(bookingId, session.getUserId()) : null;
        if (b == null) { finish(); return; }

        ((TextView) findViewById(R.id.tvStatus)).setText(b.status);
        ((TextView) findViewById(R.id.tvStatus)).setTextColor(Booking.CONFIRMED.equals(b.status)
                ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
        ((TextView) findViewById(R.id.tvRef)).setText(b.ref);
        ((TextView) findViewById(R.id.tvTitle)).setText(Service.icon(b.category) + "  " + b.title);
        ((TextView) findViewById(R.id.tvRoute)).setText(b.route());
        ((TextView) findViewById(R.id.tvDateTime)).setText(b.travelDate + " • " + b.travelTime);
        ((TextView) findViewById(R.id.tvSeats)).setText("Seats: " + b.seats);
        ((TextView) findViewById(R.id.tvAmount)).setText("Amount: " + Formats.money(b.amount));
        ((TextView) findViewById(R.id.tvPayment)).setText("Payment: " + b.paymentMode);
        ((TextView) findViewById(R.id.tvBookedAt)).setText("Booked on " + b.bookedAt);

        ImageView iv = findViewById(R.id.ivQr);
        try {
            iv.setImageBitmap(qr(b.ref + "|" + b.title + "|" + b.travelDate + " " + b.travelTime + "|" + b.seats, 600));
        } catch (WriterException e) {
            iv.setImageDrawable(null);
        }
        btnCancel.setEnabled(b.isCancellable());
        btnCancel.setAlpha(b.isCancellable() ? 1f : 0.4f);
    }

    private void cancel() {
        if (db.cancelBooking(bookingId, session.getUserId())) {
            Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "This booking can no longer be cancelled", Toast.LENGTH_SHORT).show();
        }
        load();
    }

    private Bitmap qr(String text, int size) throws WriterException {
        BitMatrix m = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size);
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                bmp.setPixel(x, y, m.get(x, y) ? Color.BLACK : Color.WHITE);
        return bmp;
    }
}

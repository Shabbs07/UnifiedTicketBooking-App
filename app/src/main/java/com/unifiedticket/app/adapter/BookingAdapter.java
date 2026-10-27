package com.unifiedticket.app.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unifiedticket.app.R;
import com.unifiedticket.app.model.Booking;
import com.unifiedticket.app.model.Service;
import com.unifiedticket.app.util.Formats;

import java.util.ArrayList;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.VH> {
    public interface Listener { void onClick(Booking b); }

    private final List<Booking> items = new ArrayList<>();
    private final Listener listener;

    public BookingAdapter(Listener listener) { this.listener = listener; }

    public void setItems(List<Booking> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Booking b = items.get(position);
        h.title.setText(Service.icon(b.category) + "  " + b.title);
        h.ref.setText(b.ref);
        h.dateTime.setText(b.travelDate + "  •  " + b.travelTime);
        h.seats.setText("Seats: " + b.seats);
        h.amount.setText(Formats.money(b.amount));
        h.status.setText(b.status);
        h.status.setTextColor(Booking.CONFIRMED.equals(b.status)
                ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
        h.itemView.setOnClickListener(v -> listener.onClick(b));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, ref, dateTime, seats, amount, status;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            ref = v.findViewById(R.id.tvRef);
            dateTime = v.findViewById(R.id.tvDateTime);
            seats = v.findViewById(R.id.tvSeats);
            amount = v.findViewById(R.id.tvAmount);
            status = v.findViewById(R.id.tvStatus);
        }
    }
}

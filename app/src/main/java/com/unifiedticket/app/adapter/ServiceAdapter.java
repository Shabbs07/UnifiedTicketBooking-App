package com.unifiedticket.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unifiedticket.app.R;
import com.unifiedticket.app.model.Service;
import com.unifiedticket.app.util.Formats;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.VH> {
    public interface Listener { void onClick(Service s); }

    private final List<Service> items = new ArrayList<>();
    private final Listener listener;

    public ServiceAdapter(Listener listener) { this.listener = listener; }

    public void setItems(List<Service> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Service s = items.get(position);
        h.title.setText(Service.icon(s.category) + "  " + s.title);
        h.route.setText(s.route());
        h.dateTime.setText(s.date + "  •  " + s.time);
        h.operator.setText(s.operator);
        h.price.setText(Formats.money(s.price));
        h.itemView.setOnClickListener(v -> listener.onClick(s));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, route, dateTime, operator, price;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            route = v.findViewById(R.id.tvRoute);
            dateTime = v.findViewById(R.id.tvDateTime);
            operator = v.findViewById(R.id.tvOperator);
            price = v.findViewById(R.id.tvPrice);
        }
    }
}

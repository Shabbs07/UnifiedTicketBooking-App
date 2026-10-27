package com.unifiedticket.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unifiedticket.app.R;
import com.unifiedticket.app.adapter.ServiceAdapter;
import com.unifiedticket.app.db.DatabaseHelper;
import com.unifiedticket.app.model.Service;

import java.util.List;

public class ServiceListActivity extends AppCompatActivity {
    public static final String EXTRA_CATEGORY = "category";

    private DatabaseHelper db;
    private ServiceAdapter adapter;
    private TextView tvEmpty;
    private String category;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);

        db = DatabaseHelper.get(this);
        category = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (category == null) category = Service.BUS;
        setTitle(Service.icon(category) + " " + Service.label(category) + " bookings");

        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServiceAdapter(s -> {
            Intent i = new Intent(this, SeatSelectionActivity.class);
            i.putExtra(SeatSelectionActivity.EXTRA_SERVICE_ID, s.id);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { load(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });
        load("");
    }

    private void load(String query) {
        List<Service> list = db.getServices(category, query);
        adapter.setItems(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}

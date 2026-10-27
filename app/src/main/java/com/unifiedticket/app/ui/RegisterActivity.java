package com.unifiedticket.app.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unifiedticket.app.R;
import com.unifiedticket.app.db.DatabaseHelper;
import com.unifiedticket.app.util.SecurityUtil;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etEmail, etPhone, etPassword, etConfirm;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Create account");
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        findViewById(R.id.btnRegister).setOnClickListener(v -> register());
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();

        if (name.length() < 2) { etName.setError("Enter your name"); return; }
        if (!SecurityUtil.isValidEmail(email)) { etEmail.setError("Enter a valid email"); return; }
        if (!SecurityUtil.isValidPhone(phone)) { etPhone.setError("Enter a valid 10-digit mobile number"); return; }
        if (!SecurityUtil.isStrongPassword(password)) {
            etPassword.setError("Min 8 characters with letters and digits");
            return;
        }
        if (!password.equals(confirm)) { etConfirm.setError("Passwords do not match"); return; }

        if (DatabaseHelper.get(this).registerUser(name, email, phone, password)) {
            Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            etEmail.setError("This email is already registered");
        }
    }
}

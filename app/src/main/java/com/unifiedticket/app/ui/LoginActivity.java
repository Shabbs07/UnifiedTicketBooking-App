package com.unifiedticket.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unifiedticket.app.R;
import com.unifiedticket.app.db.DatabaseHelper;
import com.unifiedticket.app.model.User;
import com.unifiedticket.app.util.SecurityUtil;
import com.unifiedticket.app.util.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new SessionManager(this).isLoggedIn()) {
            goHome();
            return;
        }
        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.tvRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (!SecurityUtil.isValidEmail(email)) { etEmail.setError("Enter a valid email"); return; }
        if (password.isEmpty()) { etPassword.setError("Enter your password"); return; }

        User user = DatabaseHelper.get(this).authenticate(email, password);
        if (user == null) {
            // Same message for unknown email / wrong password: do not reveal which one failed.
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            return;
        }
        new SessionManager(this).login(user);
        goHome();
    }

    private void goHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}

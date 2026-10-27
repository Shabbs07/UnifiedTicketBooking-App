package com.unifiedticket.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.unifiedticket.app.model.User;

/** Keeps only the logged-in user's id and name (no credentials). */
public class SessionManager {
    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public void login(User u) { prefs.edit().putLong("uid", u.id).putString("name", u.name).apply(); }
    public boolean isLoggedIn() { return prefs.getLong("uid", -1) != -1; }
    public long getUserId() { return prefs.getLong("uid", -1); }
    public String getName() { return prefs.getString("name", ""); }
    public void logout() { prefs.edit().clear().apply(); }
}

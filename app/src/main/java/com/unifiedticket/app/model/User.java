package com.unifiedticket.app.model;

public class User {
    public final long id;
    public final String name, email, phone;

    public User(long id, String name, String email, String phone) {
        this.id = id; this.name = name; this.email = email; this.phone = phone;
    }
}

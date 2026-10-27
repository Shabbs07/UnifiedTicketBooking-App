package com.unifiedticket.app.util;

import java.util.Locale;

public final class Formats {
    private Formats() {}
    public static String money(double v) { return String.format(Locale.US, "₹%.2f", v); }
}

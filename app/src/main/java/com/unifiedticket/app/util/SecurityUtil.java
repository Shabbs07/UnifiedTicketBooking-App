package com.unifiedticket.app.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Password hashing and input validation. Never store or log plain-text passwords. */
public final class SecurityUtil {
    private static final SecureRandom RNG = new SecureRandom();
    private static final int ITERATIONS = 100_000, KEY_BITS = 256;
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[6-9][0-9]{9}$");   // Indian mobile number
    private static final String REF_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no look-alike chars

    private SecurityUtil() {}

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** PBKDF2-HMAC-SHA256 with a per-user random salt. */
    public static String hash(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(),
                    Base64.getDecoder().decode(salt), ITERATIONS, KEY_BITS);
            byte[] out = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    /** Constant-time comparison to avoid timing side channels. */
    public static boolean verify(String password, String salt, String expectedHash) {
        byte[] a = hash(password, salt).getBytes(StandardCharsets.UTF_8);
        byte[] b = expectedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    public static boolean isValidEmail(String s) { return s != null && EMAIL.matcher(s).matches(); }

    public static boolean isValidPhone(String s) { return s != null && PHONE.matcher(s).matches(); }

    /** At least 8 characters with a letter and a digit. */
    public static boolean isStrongPassword(String s) {
        if (s == null || s.length() < 8) return false;
        boolean letter = false, digit = false;
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) letter = true;
            if (Character.isDigit(c)) digit = true;
        }
        return letter && digit;
    }

    /** Booking reference such as UTB-7K3M9QXA. */
    public static String newBookingRef() {
        StringBuilder sb = new StringBuilder("UTB-");
        for (int i = 0; i < 8; i++) sb.append(REF_CHARS.charAt(RNG.nextInt(REF_CHARS.length())));
        return sb.toString();
    }
}

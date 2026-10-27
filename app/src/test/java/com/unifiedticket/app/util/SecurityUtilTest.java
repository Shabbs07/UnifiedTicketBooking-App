package com.unifiedticket.app.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class SecurityUtilTest {

    @Test public void hashVerifiesCorrectPasswordOnly() {
        String salt = SecurityUtil.generateSalt();
        String h = SecurityUtil.hash("Secret123", salt);
        assertTrue(SecurityUtil.verify("Secret123", salt, h));
        assertFalse(SecurityUtil.verify("secret123", salt, h));
    }

    @Test public void sameSaltDifferentUsersGetDifferentHashes() {
        String h1 = SecurityUtil.hash("Secret123", SecurityUtil.generateSalt());
        String h2 = SecurityUtil.hash("Secret123", SecurityUtil.generateSalt());
        assertNotEquals(h1, h2);
    }

    @Test public void emailValidation() {
        assertTrue(SecurityUtil.isValidEmail("shabbs@example.com"));
        assertFalse(SecurityUtil.isValidEmail("not-an-email"));
        assertFalse(SecurityUtil.isValidEmail("a@b"));
    }

    @Test public void phoneValidation() {
        assertTrue(SecurityUtil.isValidPhone("9876543210"));
        assertFalse(SecurityUtil.isValidPhone("1234567890"));
        assertFalse(SecurityUtil.isValidPhone("98765"));
    }

    @Test public void passwordStrength() {
        assertTrue(SecurityUtil.isStrongPassword("abcd1234"));
        assertFalse(SecurityUtil.isStrongPassword("abcdefgh"));
        assertFalse(SecurityUtil.isStrongPassword("a1"));
    }

    @Test public void bookingRefFormat() {
        String ref = SecurityUtil.newBookingRef();
        assertTrue(ref.matches("UTB-[A-Z2-9]{8}"));
    }
}

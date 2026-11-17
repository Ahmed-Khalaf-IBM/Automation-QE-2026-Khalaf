package com.framework.utils;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomDataUtil {

    private RandomDataUtil() {}

    private static String suffix() {
        return Long.toString(System.nanoTime() % 100_000_000L) + ThreadLocalRandom.current().nextInt(10, 99);
    }

    /** Unique username (OrangeHRM max 40 chars). */
    public static String username(String prefix) {
        return prefix + "_" + suffix();
    }

    /** Meets OrangeHRM password rules: 7+ chars, upper, lower, digit. */
    public static String password() {
        return "Pw" + suffix() + "xyz";
    }

    public static String alphaNumeric(String prefix) {
        return prefix + suffix();
    }
}

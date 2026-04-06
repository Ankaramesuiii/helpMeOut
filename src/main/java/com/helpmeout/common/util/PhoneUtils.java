package com.helpmeout.common.util;

public final class PhoneUtils {

    private PhoneUtils() {}

    public static String normalizeTunisiaPhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("\\s+", "").trim();
    }
}

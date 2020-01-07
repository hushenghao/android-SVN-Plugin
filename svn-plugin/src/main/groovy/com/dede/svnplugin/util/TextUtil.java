package com.dede.svnplugin.util;

public class TextUtil {

    private TextUtil() {
    }

    public static boolean isNull(String text) {
        if (text == null) return true;

        text = text.trim();
        if (text.length() == 0) return true;

        return text.toUpperCase().equals("NULL");

    }
}

package com.lattis.ellipse.domain.utils;

import android.annotation.SuppressLint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;

import java.util.regex.Pattern;

public class StringUtils {

    public static final int NO_ERROR = -1;

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 31;

    private static final String PATTERN_LETTER_AND_NUMBER = "^(?=.*[a-zA-Z])(?=.*\\d)";
    private static final String PATTERN_ALLOWED_CHARACTERS = "^[a-zA-Z0-9!#@{~}$_^-]+$";
    private static final String PATTERN_WHITE_SPACE = "\\s";
    private static final String WORD_PASSWORD = "password";

    public static String capitalizeFully(String str) {
        StringBuilder result = new StringBuilder(str.length());
        String[] split = str.split(" ");
        for (String s : split) {
            char[] chars = s.toCharArray();
            for (int i = 1; i < chars.length; i++) {
                if (Character.isUpperCase(chars[i])) {
                    chars[i] = Character.toLowerCase(chars[i]);
                }
            }
            result.append(new String(chars));
            result.append(" ");
        }
        return result.toString();
    }

    @SuppressLint("ParcelCreator")
    private static class URLSpanNoUnderline extends URLSpan {

        URLSpanNoUnderline(String url) {
            super(url);
        }

        public void updateDrawState(TextPaint drawState) {
            super.updateDrawState(drawState);
            drawState.setUnderlineText(false);
        }
    }

    public static boolean isValidEmail(final String target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isValidPhoneNumber(final String target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.PHONE.matcher(target).matches();
    }

    public static boolean isLongerThanMinLength(String target) {
        return !TextUtils.isEmpty(target) && target.length() >= MIN_PASSWORD_LENGTH;
    }

    private static boolean isShorterThanMaxLength(String target) {
        return target.length() <= MAX_PASSWORD_LENGTH;
    }

    private static boolean isTheWordPassword(String target) {
        return target.toLowerCase().equals(WORD_PASSWORD);
    }

    private static boolean isContainingOneLetterAndOneNumber(String target) {
        return Pattern.compile(PATTERN_LETTER_AND_NUMBER).matcher(target).find();
    }

    private static boolean isContainingOnlyAllowedCharacters(String target) {
        return Pattern.compile(PATTERN_ALLOWED_CHARACTERS).matcher(target).matches();
    }

    private static boolean isContainingWhiteSpace(String target) {
        return Pattern.compile(PATTERN_WHITE_SPACE).matcher(target).find();
    }

    public static String trimMacAddress(String addressMac){
        return addressMac.replace(":","");
    }
}

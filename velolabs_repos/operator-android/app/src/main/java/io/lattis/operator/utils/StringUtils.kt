package io.lattis.operator.utils

import android.annotation.SuppressLint
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.URLSpan

import java.util.regex.Pattern

object StringUtils {

    val NO_ERROR = -1

    private val MIN_PASSWORD_LENGTH = 1
    private val MAX_PASSWORD_LENGTH = 16

    private val PATTERN_LETTER_AND_NUMBER = "^(?=.*[a-zA-Z])(?=.*\\d)"
    private val PATTERN_ALLOWED_CHARACTERS = "^[a-zA-Z0-9!#@{~}$^-]+$"
    private val PATTERN_WHITE_SPACE = "\\s"
    private val WORD_PASSWORD = "password"

    fun capitalizeFully(str: String): String {
        val result = StringBuilder(str.length)
        val split = str.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (s in split) {
            val chars = s.toCharArray()
            for (i in 1 until chars.size) {
                if (Character.isUpperCase(chars[i])) {
                    chars[i] = Character.toLowerCase(chars[i])
                }
            }
            result.append(String(chars))
            result.append(" ")
        }
        return result.toString()
    }

    @SuppressLint("ParcelCreator")
    private class URLSpanNoUnderline internal constructor(url: String) : URLSpan(url) {

        override fun updateDrawState(drawState: TextPaint) {
            super.updateDrawState(drawState)
            drawState.isUnderlineText = false
        }
    }

    fun isValidEmail(target: String?): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun isValidPhoneNumber(target: String): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.PHONE.matcher(target).matches()
    }

    fun isLongerThanMinLength(target: String?): Boolean {
        return !TextUtils.isEmpty(target) && target?.length!=null && target?.length >= MIN_PASSWORD_LENGTH
    }

    fun isShorterThanMaxLength(target: String?): Boolean {
        return !TextUtils.isEmpty(target) && target?.length!=null && target?.length <= MAX_PASSWORD_LENGTH
    }

    private fun isTheWordPassword(target: String): Boolean {
        return target.toLowerCase() == WORD_PASSWORD
    }

    private fun isContainingOneLetterAndOneNumber(target: String): Boolean {
        return Pattern.compile(PATTERN_LETTER_AND_NUMBER).matcher(target).find()
    }

    private fun isContainingOnlyAllowedCharacters(target: String): Boolean {
        return Pattern.compile(PATTERN_ALLOWED_CHARACTERS).matcher(target).matches()
    }

    private fun isContainingWhiteSpace(target: String): Boolean {
        return Pattern.compile(PATTERN_WHITE_SPACE).matcher(target).find()
    }

    fun trimMacAddress(addressMac: String): String {
        return addressMac.replace(":", "")
    }
}

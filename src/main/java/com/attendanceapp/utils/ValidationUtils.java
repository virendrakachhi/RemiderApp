package com.attendanceapp.utils;

import android.text.TextUtils;

public class ValidationUtils {
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}

package org.raspiot.raspIot.Auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by asus on 2017/9/18.
 */

public class LocalValidation {
    public static boolean isEmailValid(String email) {
        String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isPasswordValid(String password) {
        /* password's length must be between 6 and 16 characters long! */
        return password.length() > 5 && password.length() < 17;
    }
}

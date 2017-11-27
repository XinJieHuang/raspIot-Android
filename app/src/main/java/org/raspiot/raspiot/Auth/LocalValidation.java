package org.raspiot.raspiot.Auth;

import org.raspiot.raspiot.DatabaseGlobal.UserInfoDB;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.CURRENT_SERVER_ID;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.CurrentHostModeIsCloudServerMode;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.DEFAULT_CLOUD_SERVER_ADDR;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.DEFAULT_USER_INFO_ID;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.getCurrentUserInfo;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;

/**
 * Created by asus on 2017/9/18.
 */

public class LocalValidation {

    public static boolean isLogInNeed(){
        UserInfoDB userInfo = getCurrentUserInfo();
        if(userInfo.getId() == DEFAULT_USER_INFO_ID && isRaspIotCloudMode())
            return true;
        return false;
    }

    public static boolean isRaspIotCloudMode(){
        if(CurrentHostModeIsCloudServerMode())
            if(getHostAddrFromDatabase(CURRENT_SERVER_ID).equals(DEFAULT_CLOUD_SERVER_ADDR))
                return true;
        return false;
    }

    static boolean isEmailValid(String email) {
        String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    static boolean isUsernameValid(String username){
        return username.length() > 3 && username.length() < 17;
    }

    static boolean isPasswordValid(String password) {
        /* password's length must be between 6 and 16 characters long! */
        return password.length() > 5 && password.length() < 17;
    }

    static boolean isPasswordEqual(String password1, String password2){
        return password1.equals(password2);
    }
}

package org.raspiot.raspIot.Auth;

import org.raspiot.raspIot.databaseGlobal.UserInfoDB;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CLOUD_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CURRENT_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CurrentHostModeIsCloudServerMode;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.DEFAULT_CLOUD_SERVER_ADDR;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.DEFAULT_USER_INFO_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.getCurrentUserInfo;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;

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

    public static boolean isEmailValid(String email) {
        String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
        Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isUsernameValid(String username){
        return username.length() > 3 && username.length() < 17;
    }

    public static boolean isPasswordValid(String password) {
        /* password's length must be between 6 and 16 characters long! */
        return password.length() > 5 && password.length() < 17;
    }

    public static boolean isPasswordEqual(String password1, String password2){
        return password1.equals(password2);
    }
}

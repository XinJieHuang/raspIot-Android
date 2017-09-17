package org.raspiot.raspIot.databaseGlobal;

/**
 * Created by asus on 2017/9/17.
 */

public class UserInfoDB {
    private String email;
    private String name;
    private boolean authStatus;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getAuthStatus() {
        return authStatus;
    }

    public void setAuthStatus(boolean authStatus) {
        this.authStatus = authStatus;
    }
}

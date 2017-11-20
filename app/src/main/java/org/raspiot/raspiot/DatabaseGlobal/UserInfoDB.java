package org.raspiot.raspiot.DatabaseGlobal;

import org.litepal.crud.DataSupport;

/**
 * Created by asus on 2017/9/17.
 */

public class UserInfoDB extends DataSupport{
    private int id;
    private String email;
    private String username;
    private boolean authStatus;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean getAuthStatus() {
        return authStatus;
    }

    public void setAuthStatus(boolean authStatus) {
        this.authStatus = authStatus;
    }
}

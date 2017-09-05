package org.raspiot.raspIot.databaseGlobal;

import org.litepal.crud.DataSupport;

/**
 * Created by asus on 2017/8/18.
 */

public class HostAddrDB extends DataSupport{
    private int id;
    private String hostAddr;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHostAddr() {
        return hostAddr;
    }

    public void setHostAddr(String hostAddr) {
        this.hostAddr = hostAddr;
    }
}

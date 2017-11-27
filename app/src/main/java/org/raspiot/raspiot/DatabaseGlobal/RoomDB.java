package org.raspiot.raspiot.DatabaseGlobal;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2017/8/17.
 */

public class RoomDB extends DataSupport{
    private int id;
    private String name;
    private String updateTime;
    private List<DeviceDB> deviceDBList = new ArrayList<DeviceDB>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeviceDBList(List<DeviceDB> deviceDBList) {
        this.deviceDBList = deviceDBList;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public List<DeviceDB> getDevices(){
        return DataSupport.where("roomdb_id = ?", Integer.toString(id)).find(DeviceDB.class);
    }
}

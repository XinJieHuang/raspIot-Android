package org.raspiot.raspot.Room.json;

import java.util.List;

/**
 * Created by asus on 2017/8/5.
 */

public class RoomJSON {
    private String name;
    private String updateTime;
    private List<DeviceJSON> devices;

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public void setDevices(List<DeviceJSON> devices){
        this.devices = devices;
    }
    public List<DeviceJSON> getDevices(){
        return devices;
    }
}

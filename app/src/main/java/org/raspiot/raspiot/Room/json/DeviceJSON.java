package org.raspiot.raspiot.Room.json;

import java.util.List;

/**
 * Created by asus on 2017/8/5.
 */

public class DeviceJSON {
    private String name;
    private String uuid;
    private boolean status;
    private List<DeviceContentJSON> deviceContent;

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public void setUuid(String uuid){
        this.uuid = uuid;
    }
    public String getUuid(){
        return uuid;
    }

    public  void setDeviceContent(List<DeviceContentJSON> device_content){
        this.deviceContent = device_content;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<DeviceContentJSON> getDeviceContent(){
        return deviceContent;
    }
}

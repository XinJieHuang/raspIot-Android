package org.raspiot.raspot.DatabaseGlobal;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2017/8/17.
 */

public class DeviceDB extends DataSupport{
    private int id;
    private String uuid;
    private String name;
    private boolean status;
    private RoomDB roomDB;
    private List<DeviceContentDB> deviceContentDBList = new ArrayList<>();

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public String getUuid(){
        return uuid;
    }
    public void setUuid(String uuid){
        this.uuid = uuid;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public RoomDB getRoomDB() {
        return roomDB;
    }

    public void setRoomDB(RoomDB roomDB) {
        this.roomDB = roomDB;
    }

    public List<DeviceContentDB> getDeviceContentDBList() {
        return deviceContentDBList;
    }

    public void setDeviceContentDBList(List<DeviceContentDB> deviceContentDBList) {
        this.deviceContentDBList = deviceContentDBList;
    }

    public List<DeviceContentDB> getDeviceContents(){
        return DataSupport.where("devicedb_id = ?", Integer.toString(id)).find(DeviceContentDB.class);
    }
}

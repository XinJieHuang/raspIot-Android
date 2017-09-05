package org.raspiot.raspIot.databaseGlobal;

import org.litepal.crud.DataSupport;

/**
 * Created by asus on 2017/8/17.
 */

public class DeviceContentDB extends DataSupport{
    private int id;
    private String type;
    private String name;
    private String value;
    private DeviceDB deviceDB;

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public String getType(){
        return type;
    }
    public void setType(String type){
        this.type = type;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getValue(){
        return value;
    }
    public void setValue(String value){
        this.value = value;
    }

    public DeviceDB getDeviceDB() {
        return deviceDB;
    }

    public void setDeviceDB(DeviceDB deviceDB) {
        this.deviceDB = deviceDB;
    }
}

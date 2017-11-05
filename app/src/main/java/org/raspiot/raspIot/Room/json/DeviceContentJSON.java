package org.raspiot.raspiot.Room.json;

/**
 * Created by asus on 2017/8/5.
 */

public class DeviceContentJSON {
    private String type;
    private String name;
    private String value;

    public void setType(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public void setValue(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
}

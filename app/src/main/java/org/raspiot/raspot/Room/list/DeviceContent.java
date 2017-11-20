package org.raspiot.raspot.Room.list;

/**
 * Created by asus on 2017/7/31.
 */

public class DeviceContent {
    private String type;
    private String name;
    private String value;

    public DeviceContent(String type, String name, String value){
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public String getType(){
        return type;
    }

    public String getName(){
        return name;
    }

    public String getValue(){
        return value;
    }

    public void setValue(String value){
        this.value = value;
    }

    public boolean equals(Object o){
        if(this == o)
            return true;
        if(!(o instanceof DeviceContent))
            return false;

        final DeviceContent other = (DeviceContent)o;
        if(this.type.equals(other.getType()) &&this.name.equals(other.getName()) && (this.value == other.value))
            return true;
        else
            return false;
    }
}

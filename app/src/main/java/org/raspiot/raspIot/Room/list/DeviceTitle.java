package org.raspiot.raspIot.Room.list;

public class DeviceTitle {
    private String name;
    private int imageId;

    public DeviceTitle(String name, int imageId){
        this.name = name;
        this.imageId = imageId;
    }

    public  String getName(){
        return name;
    }

    public int getImageId(){
        return imageId;
    }

    public boolean equals(Object o){
        if(this == o)
            return true;
        if(!(o instanceof DeviceTitle))
            return false;

        final DeviceTitle other = (DeviceTitle)o;
        if(this.name.equals(other.getName()) && (this.imageId == other.imageId))
            return true;
        else
            return false;
    }
}

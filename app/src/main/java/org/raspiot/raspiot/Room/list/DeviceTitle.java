package org.raspiot.raspot.Room.list;

public class DeviceTitle {
    private String name;
    private int imageId;
    private String status;

    public DeviceTitle(String name, int imageId, boolean status){
        this.name = name;
        this.imageId = imageId;
        setStatus(status);
    }
    public  String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageId(){
        return imageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        if(status == true)
            this.status = "online";
        else if(status == false)
            this.status = "offline";
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

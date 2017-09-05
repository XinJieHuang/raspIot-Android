package org.raspiot.raspIot.Home.list;

/**
 * Created by asus on 2017/5/18.
 */

public class Room {
    private String name;
    private int imageId;

    public Room(String name, int imageId){
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
        if(!(o instanceof Room))
            return false;

        final Room other = (Room)o;
        if(this.name.equals(other.getName()) && (this.imageId == other.imageId))
            return true;
        else
            return false;
    }

}

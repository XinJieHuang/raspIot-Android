package org.raspiot.raspot.Room.list;

import java.util.List;

/**
 * Created by asus on 2017/7/31.
 */

public class Device<K extends DeviceTitle, V extends DeviceContent> {
    private K groupItem;
    private List<V> subItems;

    public Device(K groupItem, List<V> subItems){
        this.groupItem = groupItem;
        this.subItems = subItems;
    }

    public K getGroupItem(){
        return groupItem;
    }

    public List<V> getSubItems(){
        return subItems;
    }

    public boolean equals(Object o){
        if(this == o)
            return true;
        if(!(o instanceof Device))
            return false;

        final Device other = (Device)o;
        /*Use the 'equals' method of DeviceTitle  and DeviceContent */
        if(this.groupItem.equals(other.getGroupItem()) && (this.subItems.equals(other.getSubItems())))
            return true;
        else
            return false;
    }
}

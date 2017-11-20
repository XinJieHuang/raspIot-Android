package org.raspiot.raspot.Room;

import org.litepal.crud.DataSupport;
import org.raspiot.raspot.R;
import org.raspiot.raspot.DatabaseGlobal.DeviceContentDB;
import org.raspiot.raspot.DatabaseGlobal.DeviceDB;
import org.raspiot.raspot.DatabaseGlobal.RoomDB;
import org.raspiot.raspot.Room.json.DeviceContentJSON;
import org.raspiot.raspot.Room.json.DeviceJSON;
import org.raspiot.raspot.Room.json.RoomJSON;
import org.raspiot.raspot.Room.list.Device;
import org.raspiot.raspot.Room.list.DeviceContent;
import org.raspiot.raspot.Room.list.DeviceTitle;

import java.util.ArrayList;
import java.util.List;

import static org.raspiot.raspot.Room.RoomActivity.roomName;
import static org.raspiot.raspot.DatabaseGlobal.DatabaseCommonOperations.STANDARD_INITIAL_TIME;

/**
 * Created by asus on 2017/8/26.
 */

public class RoomDatabaseHandler {
    protected static void getDeviceDataFromDatabase(List<Device> deviceList){
        List<DeviceDB> deviceDBList;
        try{
            deviceDBList = DataSupport           //连缀查询
                    .where("name = ?", roomName)     //查找对应房间
                    .findFirst(RoomDB.class)
                    .getDevices();                 //获取该房间的设备List
        }catch (NullPointerException e){
            return;
        }

        for(DeviceDB deviceDB : deviceDBList){
            DeviceTitle deviceTitle = new DeviceTitle(deviceDB.getName(), R.drawable.recyclerview_item_image, deviceDB.getStatus());
            List<DeviceContentDB> deviceContentDBList = deviceDB.getDeviceContents();

            List<DeviceContent> deviceContentList = new ArrayList<>();
            for(DeviceContentDB deviceContentDB : deviceContentDBList){
                DeviceContent deviceContent = new DeviceContent(deviceContentDB.getType(),
                        deviceContentDB.getName(),
                        deviceContentDB.getValue());
                deviceContentList.add(deviceContent);
            }
            deviceList.add(new Device(deviceTitle, deviceContentList));
        }
    }

    protected static String getLastUpdateTimeFromDatabase(){
        RoomDB roomDB = DataSupport
                .where("name = ?", roomName)
                .findFirst(RoomDB.class);
        if(roomDB != null)
            return roomDB.getUpdateTime();
        else
            return STANDARD_INITIAL_TIME; //2015-12-17 22:22:00
    }


    protected static void parseDeviceDataAndSaveToDatabase(RoomJSON roomJSON){
        if(roomJSON == null || !roomJSON.getName().equals(roomName))
            return;
        /* *******************************Room********************************** */
        RoomDB roomDBCheck;       //To check whether or not the roomDB data exists in database
        RoomDB roomDB = new RoomDB();
        roomDB.setName(roomName);
        roomDB.setUpdateTime(roomJSON.getUpdateTime());
        roomDB.setId(0);            //default: insert into table
        if((roomDBCheck = DataSupport.where("name = ?", roomDB.getName()).findFirst(RoomDB.class)) != null) {
            roomDB.setId(roomDBCheck.getId());
        }

        /*###########################Device List#################################*/
        List<DeviceJSON> deviceJSONList = roomJSON.getDevices();
        List<DeviceDB> deviceDBList = new ArrayList<>();
        for(DeviceJSON deviceJSON : deviceJSONList){
            DeviceDB deviceDBCheck;      //To check whether or not the deviceDB data exists in database
            DeviceDB deviceDB = new DeviceDB();
            deviceDB.setName(deviceJSON.getName());
            deviceDB.setUuid(deviceJSON.getUuid());
            deviceDB.setStatus(deviceJSON.getStatus());
            deviceDB.setId(0);
            if((deviceDBCheck = DataSupport.where("name = ? and roomdb_id = ?", deviceDB.getName(), Integer.toString(roomDB.getId())).findFirst(DeviceDB.class)) != null) {
                deviceDB.setId(deviceDBCheck.getId());
            }
            /* if device's status is false, means couldn't get deviceContent from iotServer*/
            if(deviceDB.getStatus() == true) {
                /*%%%%%%%%%%%%%%%%%%%%%%%%DeviceContent List%%%%%%%%%%%%%%%%%%%%%%%%%*/
                List<DeviceContentJSON> deviceContentJSONList = deviceJSON.getDeviceContent();
                List<DeviceContentDB> deviceContentDBList = new ArrayList<>();
                for (DeviceContentJSON deviceContentJSON : deviceContentJSONList) {
                    DeviceContentDB deviceContentDB = new DeviceContentDB();
                    deviceContentDB.setType(deviceContentJSON.getType());
                    deviceContentDB.setName(deviceContentJSON.getName());
                    deviceContentDB.setValue(deviceContentJSON.getValue());
                    deviceContentDB.saveOrUpdate("name = ? and devicedb_id = ?", deviceContentDB.getName(), Integer.toString(deviceDB.getId()));
                    deviceContentDBList.add(deviceContentDB);
                }
                /*%%%%%%%%%%%%%%%%%%%%%%%%All DeviceContent%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
                deviceDB.setDeviceContentDBList(deviceContentDBList);
            }
            if(deviceDB.getStatus() == false)   /*LitePal not support set a value to default*/
                deviceDB.setToDefault("status");
            deviceDB.saveOrUpdate("name = ? and roomdb_id = ?", deviceDB.getName(), Integer.toString(roomDB.getId()));
            deviceDBList.add(deviceDB);
        }
        /*############################All Device#################################*/
        roomDB.setDeviceDBList(deviceDBList);
        roomDB.saveOrUpdate("name = ?", roomDB.getName());
        /* ************************************Room******************************** */
    }
}

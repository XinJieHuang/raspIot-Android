package org.raspiot.raspIot.Room;

import org.litepal.crud.DataSupport;
import org.raspiot.raspIot.R;
import org.raspiot.raspIot.databaseGlobal.DeviceContentDB;
import org.raspiot.raspIot.databaseGlobal.DeviceDB;
import org.raspiot.raspIot.databaseGlobal.RoomDB;
import org.raspiot.raspIot.Room.json.DeviceContentJSON;
import org.raspiot.raspIot.Room.json.DeviceJSON;
import org.raspiot.raspIot.Room.json.RoomJSON;
import org.raspiot.raspIot.Room.list.Device;
import org.raspiot.raspIot.Room.list.DeviceContent;
import org.raspiot.raspIot.Room.list.DeviceTitle;

import java.util.ArrayList;
import java.util.List;

import static org.raspiot.raspIot.Room.RoomActivity.roomName;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.STANDARD_INITIAL_TIME;

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
            deviceDBList = new ArrayList<>();
        }

        for(DeviceDB deviceDB : deviceDBList){
            DeviceTitle deviceTitle = new DeviceTitle(deviceDB.getName(), R.drawable.item_image);
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
            deviceDB.setId(0);
            if((deviceDBCheck = DataSupport.where("name = ? and roomdb_id = ?", deviceDB.getName(), Integer.toString(roomDB.getId())).findFirst(DeviceDB.class)) != null) {
                deviceDB.setId(deviceDBCheck.getId());
            }

            /*%%%%%%%%%%%%%%%%%%%%%%%%DeviceContent List%%%%%%%%%%%%%%%%%%%%%%%%%*/
            List<DeviceContentJSON> deviceContentJSONList = deviceJSON.getDeviceContent();
            List<DeviceContentDB> deviceContentDBList = new ArrayList<>();
            for(DeviceContentJSON deviceContentJSON : deviceContentJSONList){
                DeviceContentDB deviceContentDB = new DeviceContentDB();
                deviceContentDB.setType(deviceContentJSON.getType());
                deviceContentDB.setName(deviceContentJSON.getName());
                deviceContentDB.setValue(deviceContentJSON.getValue());
                deviceContentDB.saveOrUpdate("name = ? and devicedb_id = ?", deviceContentDB.getName(), Integer.toString(deviceDB.getId()));
                deviceContentDBList.add(deviceContentDB);
            }
            /*%%%%%%%%%%%%%%%%%%%%%%%%All DeviceContent%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
            deviceDB.setDeviceContentDBList(deviceContentDBList);
            deviceDB.saveOrUpdate("name = ? and roomdb_id = ?", deviceDB.getName(), Integer.toString(roomDB.getId()));
            deviceDBList.add(deviceDB);
        }
        /*############################All Device#################################*/
        roomDB.setDeviceDBList(deviceDBList);
        roomDB.saveOrUpdate("name = ?", roomDB.getName());
        /* ************************************Room******************************** */
    }
}

package org.raspiot.raspiot.Home;

import org.litepal.crud.DataSupport;
import org.raspiot.raspiot.Room.json.RoomJSON;
import org.raspiot.raspiot.DatabaseGlobal.RoomDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2017/8/27.
 */

public class RoomDatabaseHandler {
    static void parseRoomDataAndSaveToDatabase(List<RoomJSON> roomJSONList) {
        if(roomJSONList == null)
            return;

        /* ***     get all room name from roomJSONList   *** */
        List<String> roomJSONNameList = new ArrayList<>();
        List<String> roomJSONNameList0 = new ArrayList<>();
        if(!roomJSONList.isEmpty())
            for( RoomJSON roomJSON : roomJSONList)
                roomJSONNameList.add(roomJSON.getName());
        roomJSONNameList0.addAll(roomJSONNameList);

        /* ***     get all room name from roomDBList   *** */
        List<String> roomDBNameList = new ArrayList<>();
        List<RoomDB> roomDBList = DataSupport.findAll(RoomDB.class);
        if(!roomDBList.isEmpty())
            for(RoomDB roomDB : roomDBList)
                roomDBNameList.add(roomDB.getName());

        /* roomJSONNameList just left new room  */
        roomJSONNameList.removeAll(roomDBNameList);
        /* roomDBNameList just left those room that should be deleted*/
        roomDBNameList.removeAll(roomJSONNameList0);

        if(!roomJSONNameList.isEmpty())
            for (String roomName : roomJSONNameList){
                    RoomJSON roomJSON = roomJSONList.get(roomJSONNameList0.indexOf(roomName));
                    RoomDB roomDB = new RoomDB();
                    roomDB.setName(roomJSON.getName());
                    roomDB.setUpdateTime(roomJSON.getUpdateTime());
                    roomDB.save();
                }
        if(!roomDBNameList.isEmpty())
            for(String roomName : roomDBNameList){
                DataSupport.deleteAll(RoomDB.class, "name = ?", roomName);
            }
    }

    public static List<String> getRestRoomList(String... roomNameList){
        List<RoomDB> roomDBList = getAllRoomDataFromDatabase();
        List<String> roomList = new ArrayList<>();
        if(roomDBList == null)
            return roomList;
        for(RoomDB roomDB : roomDBList){
            roomList.add(roomDB.getName());
        }
        for(String roomName : roomNameList)
            roomList.remove(roomName);
        return roomList;
    }


    public static List<RoomDB> getAllRoomDataFromDatabase(){
        return DataSupport.findAll(RoomDB.class);
    }


    public static void deleteRoomFromDatabase(String roomName){
        DataSupport.deleteAll(RoomDB.class, "name = ?", roomName);
    }
}

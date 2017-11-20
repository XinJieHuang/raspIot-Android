package org.raspiot.raspot.Home;

import org.litepal.crud.DataSupport;
import org.raspiot.raspot.Room.json.RoomJSON;
import org.raspiot.raspot.DatabaseGlobal.RoomDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2017/8/27.
 */

public class HomeDatabaseHandler {
    protected static void parseRoomDataAndSaveToDatabase(List<RoomJSON> roomJSONList) {
        if(roomJSONList == null)
            return;

        /* ***     get all room name from roomJSONList   *** */
        List<String> roomJSONNameList = new ArrayList<>();
        List<String> roomJSONnameList0 = new ArrayList<>();
        if(!roomJSONList.isEmpty())
            for( RoomJSON roomJSON : roomJSONList)
                roomJSONNameList.add(roomJSON.getName());
        roomJSONnameList0.addAll(roomJSONNameList);

        /* ***     get all room name from roomDBList   *** */
        List<String> roomDBNameList = new ArrayList<>();
        List<RoomDB> roomDBList = DataSupport.findAll(RoomDB.class);
        if(!roomDBList.isEmpty())
            for(RoomDB roomDB : roomDBList)
                roomDBNameList.add(roomDB.getName());

        /* roomJSONNameList just left new room  */
        roomJSONNameList.removeAll(roomDBNameList);
        /* roomDBNameList just left those room that should be deleted*/
        roomDBNameList.removeAll(roomJSONnameList0);

        if(!roomJSONNameList.isEmpty())
            for (String roomName : roomJSONNameList){
                    RoomJSON roomJSON = roomJSONList.get(roomJSONnameList0.indexOf(roomName));
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


    protected static List<RoomDB> getAllRoomDataFromDatabase(){
        List<RoomDB> roomDBList = DataSupport.findAll(RoomDB.class);
        return roomDBList;
    }


    public static void deleteRoomFromDatabase(String roomName){
        DataSupport.deleteAll(RoomDB.class, "name = ?", roomName);
    }
}

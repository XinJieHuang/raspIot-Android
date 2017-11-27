package org.raspiot.raspiot.Home;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.raspiot.raspiot.Room.json.RoomJSON;

import java.util.List;

/**
 * Created by asus on 2017/8/27.
 */

class HomeJSONHandler {
    static List<RoomJSON> parseRoomJSONListWithGSON(String jsonData){
        Gson gson = new Gson();
        List<RoomJSON> roomJSONList = null;
        try {                       //避免非法json数据导致程序崩溃
            roomJSONList = gson.fromJson(jsonData, new TypeToken<List<RoomJSON>>(){}.getType());
        }catch (Exception e){
            e.printStackTrace();
        }
        return roomJSONList;
    }
}

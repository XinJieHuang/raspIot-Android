package org.raspiot.raspiot.Room;

import com.google.gson.Gson;

import org.raspiot.raspiot.Room.json.RoomJSON;

/**
 * Created by asus on 2017/8/27.
 */

class RoomJSONHandler {

    static RoomJSON parseJSONWithGSON(String jsonData){
        Gson gson = new Gson();
        RoomJSON roomJSON = null;
        try {                       //避免非法json数据导致程序崩溃
            roomJSON = gson.fromJson(jsonData, RoomJSON.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return roomJSON;
    }
}

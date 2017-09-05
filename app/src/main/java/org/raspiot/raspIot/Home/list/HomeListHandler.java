package org.raspiot.raspIot.Home.list;

import org.raspiot.raspIot.R;
import org.raspiot.raspIot.Room.RoomActivity;
import org.raspiot.raspIot.Room.json.RoomJSON;
import org.raspiot.raspIot.Room.list.DeviceAdapter;
import org.raspiot.raspIot.databaseGlobal.RoomDB;
import org.raspiot.raspIot.jsonGlobal.ControlMessageJSON;
import org.raspiot.raspIot.networkGlobal.TCPClient;
import org.raspiot.raspIot.networkGlobal.ThreadCallbackListener;

import java.util.List;

import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CURRENT_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;
import static org.raspiot.raspIot.jsonGlobal.JsonCommonOperations.buildJSON;

/**
 * Created by asus on 2017/8/27.
 */

public class HomeListHandler {

    private static boolean TrueOrFale;

    /*do not use adapter.notifyDataSetChanged(); in every way I can*/
    public static void updateRoomListAndNotifyItem(List<RoomDB> roomDBList, List<Room> roomList, RoomAdapter adapter){
        if(roomDBList == null)
            return;

        /*length = max(roomList.size(), roomJSONList.size())*/
        for(int i = 0, length = roomList.size() > roomDBList.size() ? roomList.size() : roomDBList.size();i < length; i++) {

            /*Means roomJSONList.size() > roomList.size()*/
            if(i == roomList.size()){
                for(int j = i; j < length; j++)
                    roomList.add(new Room(roomDBList.get(j).getName(), R.drawable.item_image));
                /*Add elements*/
                adapter.notifyItemRangeInserted(i, roomList.size() - i);
                /*i still small than length, but roomList.size() == length*/
                break;
            }

            /*Means roomList.size() > roomJSONList.size()*/
            else if(i == roomDBList.size()){
                int j;
                /*roomList.size() is reducing*/
                for(j = i; i < roomList.size(); j++)
                    roomList.remove(i);
                /*Remove elements*/
                adapter.notifyItemRangeRemoved(i, j);
                /*i still small than length, but max(roomList.size(), roomJSONList.size()) small than length at this time*/
                break;
            }

            /* Only when i < min(roomList.size(), roomJSONList.size()) */
            else {
                Room room = new Room(roomDBList.get(i).getName(), R.drawable.item_image);
                if (roomList.get(i).equals(room))
                    continue;
                else {
                    roomList.set(i, room);
                    adapter.notifyItemChanged(i);
                }
            }
        }
    }


    public static boolean canRemoveItemOrNot(String roomName){
        String addr = getHostAddrFromDatabase(CURRENT_SERVER_ID);
        String ip = addr.split(":")[0];
        int port = Integer.parseInt(addr.split(":")[1]);
        ControlMessageJSON deleteRoomCmd = new ControlMessageJSON("del", "room", roomName);
        String data = buildJSON(deleteRoomCmd);
        TCPClient.tcpClient(ip, port, data, new ThreadCallbackListener() {
            @Override
            public void onFinish(String response) {
                TrueOrFale = true;
            }
            @Override
            public void onError(Exception e) {
                TrueOrFale = false;
                e.printStackTrace();
            }
        });
        return TrueOrFale;
    }

}

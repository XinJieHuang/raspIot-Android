package org.raspiot.raspIot.Room;

import org.raspiot.raspIot.jsonGlobal.ControlMessageJSON;
import org.raspiot.raspIot.networkGlobal.TCPClient;
import org.raspiot.raspIot.networkGlobal.ThreadCallbackListener;

import static org.raspiot.raspIot.Room.RoomActivity.roomName;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CURRENT_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;
import static org.raspiot.raspIot.jsonGlobal.JsonCommonOperations.buildJSON;

/**
 * Created by asus on 2017/9/9.
 */

public class RoomListHandler {
    private static boolean TrueOrFale;
    public static boolean setDeviceContentToValue(String deviceName, String deviceContentName, String newValue){
        String target = "deviceContent:" + roomName + "/" + deviceName + "/" + deviceContentName;
        ControlMessageJSON setDeviceContentToNewValue = new ControlMessageJSON("set", target, newValue);

        String addr = getHostAddrFromDatabase(CURRENT_SERVER_ID);
        String ip = addr.split(":")[0];
        int port = Integer.parseInt(addr.split(":")[1]);

        String data = buildJSON(setDeviceContentToNewValue);
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

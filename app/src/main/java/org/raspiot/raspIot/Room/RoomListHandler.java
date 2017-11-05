package org.raspiot.raspiot.Room;

import org.raspiot.raspiot.JsonGlobal.ControlMessage;
import org.raspiot.raspiot.NetworkGlobal.TCPClient;
import org.raspiot.raspiot.NetworkGlobal.ThreadCallbackListener;

import static org.raspiot.raspiot.Room.RoomActivity.roomName;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.CURRENT_SERVER_ID;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;
import static org.raspiot.raspiot.JsonGlobal.JsonCommonOperations.buildJSON;

/**
 * Created by asus on 2017/9/9.
 */

public class RoomListHandler {
    private static boolean TrueOrFale;
    public static boolean setValueToDeviceContent(String deviceName, String deviceContentName, String newValue){
        String target = "deviceContent:" + roomName + "/" + deviceName + "/" + deviceContentName;
        ControlMessage setDeviceContentToNewValue = new ControlMessage("set", target, newValue);

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

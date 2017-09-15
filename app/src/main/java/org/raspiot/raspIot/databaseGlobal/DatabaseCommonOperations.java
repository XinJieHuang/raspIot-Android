package org.raspiot.raspIot.databaseGlobal;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

/**
 * Created by asus on 2017/8/24.
 */

public class DatabaseCommonOperations {

    public static final int CLOUD_SERVER_ID = 1;
    public static final int RASP_SERVER_ID = 2;
    public static final int CURRENT_SERVER_ID = 3;
    public static final String STANDARD_INITIAL_TIME = "2015-12-17 22:22:00";
    public static final String DEFAULT_CLOUD_SERVER_ADDR = "www.raspiot.org";
    public static final String DEFAULT_RASP_SERVER_ADDR = "192.168.17.1:22015";

    public static boolean CurrentHostModeIsCloudServerMode(){
        HostAddrDB hostAddrDB = DataSupport.find(HostAddrDB.class, CURRENT_SERVER_ID);
        if(getHostAddrFromDatabase(CLOUD_SERVER_ID).equals(hostAddrDB.getHostAddr()))
            return true;
        else
            return false;
    }

    public static  String getHostAddrFromDatabase(int serverId){
        HostAddrDB hostAddrDB = DataSupport.find(HostAddrDB.class, serverId);
        return hostAddrDB.getHostAddr();
    }



    /*
    * Host Addr database
    */
    public static void initHostAddrDatabase(){

        Connector.getDatabase();
        HostAddrDB cloudServerAddr = new HostAddrDB();
        if(DataSupport.find(HostAddrDB.class, CLOUD_SERVER_ID) == null) { //cloudServer_id 1
            cloudServerAddr.setHostAddr(DEFAULT_CLOUD_SERVER_ADDR);            //default
            cloudServerAddr.save();
        }

        HostAddrDB raspServerAddr = new HostAddrDB();
        if(DataSupport.find(HostAddrDB.class, RASP_SERVER_ID) == null) { // raspServer_id 2
            raspServerAddr.setHostAddr(DEFAULT_RASP_SERVER_ADDR);        //default
            raspServerAddr.save();
        }

        HostAddrDB currentServerAddr = new HostAddrDB();
        if(DataSupport.find(HostAddrDB.class, CURRENT_SERVER_ID) == null){  //currentServer_id 3
            currentServerAddr.setHostAddr(DEFAULT_CLOUD_SERVER_ADDR);     //default: Cloud server mode
            currentServerAddr.save();
        }

    }

}

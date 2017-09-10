package org.raspiot.raspIot.Room;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.raspiot.raspIot.R;
import org.raspiot.raspIot.jsonGlobal.ControlMessageJSON;
import org.raspiot.raspIot.Room.json.RoomJSON;
import org.raspiot.raspIot.Room.list.Device;
import org.raspiot.raspIot.Room.list.DeviceAdapter;
import org.raspiot.raspIot.networkGlobal.HttpUtil;
import org.raspiot.raspIot.networkGlobal.TCPClient;
import org.raspiot.raspIot.networkGlobal.ThreadCallbackListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

import static org.raspiot.raspIot.Room.RoomDatabaseHandler.getDeviceDataFromDatabase;
import static org.raspiot.raspIot.Room.RoomDatabaseHandler.getLastUpdateTimeFromDatabase;
import static org.raspiot.raspIot.Room.RoomDatabaseHandler.parseDeviceDataAndSaveToDatabase;
import static org.raspiot.raspIot.Room.RoomJSONHandler.parseJSONWithGSON;
import static org.raspiot.raspIot.UICommonOperations.ToastShow.ToastShowInBottom;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CLOUD_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CURRENT_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.RASP_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CurrentHostModeIsCloudServerMode;
import static org.raspiot.raspIot.jsonGlobal.JsonCommonOperations.buildJSON;


public class RoomActivity extends AppCompatActivity {

    private DeviceAdapter adapter;
    private List<Device> deviceList;
    private SwipeRefreshLayout swipeRefresh;
    private String dataFromNetworkResponse;

    protected static String roomName;
    private final int ADD_DEVICE_SUCCEED = 2;
    private final int ADD_DEVICE_ERROR = 3;
    private final int GET_DEVICE_LIST_SUCCEED = 1;
    private final int GET_DEVICE_LIST_ERROR = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        roomName = getIntent().getStringExtra("room_name");

        initToolbar();
        initUIWidget();

        /*Main list*/
        initDevices();  //初始化Iot设备
        initRecyclerView();
    }

    //Toolbar右上角菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(!roomName.equals("Unauthorized devices"))
            getMenuInflater().inflate(R.menu.room_toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.room_add_device:
                ControlMessageJSON getDeviceListCmd = new ControlMessageJSON("add", "device:"+roomName+"/Smart light", "5c:cf:7f:14:73:a7");
                sendRequestWithSocket(getHostAddrFromDatabase(CURRENT_SERVER_ID), buildJSON(getDeviceListCmd));
                break;
            default:
        }
        return true;
    }


    //异步消息处理
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_DEVICE_LIST_SUCCEED:   //FROM SERVER
                    //获取消息 更新UI
                    // change UI here
                    RoomJSON roomJSON = parseJSONWithGSON(dataFromNetworkResponse);
                    parseDeviceDataAndSaveToDatabase(roomJSON);

                    deviceList.clear();
                    getDeviceDataFromDatabase(deviceList);

                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                    ToastShowInBottom("Refresh finish.");
                    break;

                case GET_DEVICE_LIST_ERROR:
                    swipeRefresh.setRefreshing(false);
                    ToastShowInBottom("Refresh error.");
                    break;

                case ADD_DEVICE_SUCCEED:
                    ToastShowInBottom("Add device succeed.\nWaiting for device access in.");
                    break;

                case ADD_DEVICE_ERROR:
                    ToastShowInBottom("Device already exists");
                    break;

                default:
            }
        }
    };


    private void initDevices(){
        deviceList = new ArrayList<>();
        getDeviceDataFromDatabase(deviceList);
    }



    private void refreshdevices(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getDeviceListWithNetwork();
                    }
                });
            }
        }).start();
    }


    private void getDeviceListWithNetwork(){
        if(CurrentHostModeIsCloudServerMode())
            sendRequestWithOkHttp(getHostAddrFromDatabase(CLOUD_SERVER_ID)+"/api/"+roomName+"/getdevicelist");
        else {
            ControlMessageJSON getDeviceListCmd = new ControlMessageJSON("get", "device:"+roomName, "devicelist", getLastUpdateTimeFromDatabase());
            sendRequestWithSocket(getHostAddrFromDatabase(RASP_SERVER_ID), buildJSON(getDeviceListCmd));
        }
    }


    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.room_toolbar);
        toolbar.setTitle(roomName);
        setSupportActionBar(toolbar);
        /*Add back icon*/
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initRecyclerView(){
        RecyclerView deviceRecyclerView = (RecyclerView) findViewById(R.id.devices_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        deviceRecyclerView.setLayoutManager(layoutManager);
        adapter = new DeviceAdapter(deviceList);
        deviceRecyclerView.setAdapter(adapter);
    }

    private void initUIWidget(){
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.devices_swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh(){
                refreshdevices();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }




    private void sendRequestWithSocket(String addr, String data){
        String ip = addr.split(":")[0];
        int port = Integer.parseInt(addr.split(":")[1]);
        TCPClient.tcpClient(ip, port, data, new ThreadCallbackListener() {
            @Override
            public void onFinish(String response) {
                onNetworkResponse(response);
            }
            @Override
            public void onError(Exception e) {
                onNetworkError();
                e.printStackTrace();
            }
        });
    }



    private void sendRequestWithOkHttp(String addr){
        HttpUtil.sendOkHttpRequest(addr, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //Each letter must correspond
                onNetworkResponse(response.body().string());
            }
            @Override
            public void onFailure(Call call, IOException e){
                onNetworkError();
                e.printStackTrace();
            }
        });
    }

    private void onNetworkResponse(String response){
        Message message = new Message();
        if(!response.equals("")) {
            if(response.equals("Add device succeed."))
                message.what = ADD_DEVICE_SUCCEED;
            else if(response.equals("Device already exists"))
                message.what = ADD_DEVICE_ERROR;
            else if(response.length() > 40){
                dataFromNetworkResponse = response;
                message.what = GET_DEVICE_LIST_SUCCEED;
            }
            handler.sendMessage(message);
        }else{
            onNetworkError();
        }
    }

    private void onNetworkError(){
        Message message = new Message();
        message.what = GET_DEVICE_LIST_ERROR;
        handler.sendMessage(message);
    }
}

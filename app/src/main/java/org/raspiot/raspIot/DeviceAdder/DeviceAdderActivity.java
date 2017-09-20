package org.raspiot.raspIot.DeviceAdder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.raspiot.raspIot.R;
import org.raspiot.raspIot.jsonGlobal.ControlMessage;
import org.raspiot.raspIot.networkGlobal.HttpUtil;
import org.raspiot.raspIot.networkGlobal.TCPClient;
import org.raspiot.raspIot.networkGlobal.ThreadCallbackListener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

import static org.raspiot.raspIot.UICommonOperations.ToastShow.ToastShowInBottom;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CLOUD_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CurrentHostModeIsCloudServerMode;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.RASP_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;
import static org.raspiot.raspIot.jsonGlobal.JsonCommonOperations.buildJSON;

public class DeviceAdderActivity extends AppCompatActivity {

    private EditText editDeviceName;
    private EditText editDeviceUuid;

    protected static String roomName;
    private final int ADD_DEVICE_SUCCEED = 1;
    private final int ADD_DEVICE_ERROR = -1;
    private final int NETWORK_ERROR = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_adder);
        roomName = getIntent().getStringExtra("room_name");

        initToolbar();
        initUIWidget();
    }


    //Toolbar右上角菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.device_adder_toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.device_adder_ok:
                addDeviceWithNetwork();
                break;
            default:
        }
        return true;
    }


    //异步消息处理
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD_DEVICE_SUCCEED:
                    ToastShowInBottom("Add device succeed.\nWaiting for device access in.");
                    finish();
                    break;

                case ADD_DEVICE_ERROR:
                    ToastShowInBottom("Device already exists");
                    break;

                case NETWORK_ERROR:
                    ToastShowInBottom("Network error");
                    break;
                default:
            }
        }
    };


    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.device_adder_toolbar);
        toolbar.setTitle("add device");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.toolbar_icon_cancel);
        }
    }

    private void initUIWidget(){
        editDeviceUuid = (EditText) findViewById(R.id.edit_device_adder_device_uuid);
        editDeviceName = (EditText) findViewById(R.id.edit_device_adder_device_name);

    }


    private void addDeviceWithNetwork(){
        String deviceName = editDeviceName.getText().toString();
        String deviceUuid = editDeviceUuid.getText().toString();
        ControlMessage addDevice = new ControlMessage("add", "device:"+roomName+"/"+deviceName, deviceUuid);
        String addDeviceJson = buildJSON(addDevice);
        if(CurrentHostModeIsCloudServerMode()){
            sendRequestWithOkHttp(getHostAddrFromDatabase(CLOUD_SERVER_ID)+"/api/relay", addDeviceJson);
        }else{
            sendRequestWithSocket(getHostAddrFromDatabase(RASP_SERVER_ID), addDeviceJson);
        }
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

    private void sendRequestWithOkHttp(String hostAddr, String data){
        HttpUtil.sendOkHttpRequest(hostAddr, data, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
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
            else if(response.equals("Device already exists."))
                message.what = ADD_DEVICE_ERROR;
                handler.sendMessage(message);
        }else{
            onNetworkError();
        }
    }

    private void onNetworkError(){
        Message message = new Message();
        message.what = NETWORK_ERROR;
        handler.sendMessage(message);
    }

}

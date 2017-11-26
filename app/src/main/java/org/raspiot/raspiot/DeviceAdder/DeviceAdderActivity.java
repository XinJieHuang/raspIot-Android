package org.raspiot.raspiot.DeviceAdder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.acker.simplezxing.activity.CaptureActivity;

import org.raspiot.raspiot.R;
import org.raspiot.raspiot.JsonGlobal.ControlMessage;
import org.raspiot.raspiot.NetworkGlobal.HttpUtil;
import org.raspiot.raspiot.NetworkGlobal.TCPClient;
import org.raspiot.raspiot.NetworkGlobal.ThreadCallbackListener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

import static org.raspiot.raspiot.UICommonOperations.ReminderShow.ToastShowInBottom;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.CLOUD_SERVER_ID;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.CurrentHostModeIsCloudServerMode;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.RASP_SERVER_ID;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;
import static org.raspiot.raspiot.JsonGlobal.JsonCommonOperations.buildJSON;

public class DeviceAdderActivity extends AppCompatActivity {

    private EditText editDeviceName;
    private EditText editDeviceUuid;

    protected static String roomName;
    private final int ADD_DEVICE_SUCCEED = 1;
    private final int ADD_DEVICE_ERROR = -1;
    private final int NETWORK_ERROR = -2;
    private static final int REQ_CODE_PERMISSION = 0x1111;

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
        Button btn = (Button) findViewById(R.id.btn_device_adder_qr_code);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //启动 扫描
                if(ContextCompat.checkSelfPermission(DeviceAdderActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(DeviceAdderActivity.this, new String[]{Manifest.permission.CAMERA}, REQ_CODE_PERMISSION);
                }else {
                    startCaptureActivityForResult();
                }
            }
        });
    }

    private void startCaptureActivityForResult() {
        Intent intent = new Intent(DeviceAdderActivity.this, CaptureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(CaptureActivity.KEY_NEED_BEEP, CaptureActivity.VALUE_BEEP);
        bundle.putBoolean(CaptureActivity.KEY_NEED_VIBRATION, CaptureActivity.VALUE_VIBRATION);
        bundle.putBoolean(CaptureActivity.KEY_NEED_EXPOSURE, CaptureActivity.VALUE_NO_EXPOSURE);
        bundle.putByte(CaptureActivity.KEY_FLASHLIGHT_MODE, CaptureActivity.VALUE_FLASHLIGHT_OFF);
        bundle.putByte(CaptureActivity.KEY_ORIENTATION_MODE, CaptureActivity.VALUE_ORIENTATION_AUTO);
        bundle.putBoolean(CaptureActivity.KEY_SCAN_AREA_FULL_SCREEN, CaptureActivity.VALUE_SCAN_AREA_FULL_SCREEN);
        bundle.putBoolean(CaptureActivity.KEY_NEED_SCAN_HINT_TEXT, CaptureActivity.VALUE_SCAN_HINT_TEXT);
        intent.putExtra(CaptureActivity.EXTRA_SETTING_BUNDLE, bundle);
        startActivityForResult(intent, CaptureActivity.REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_CODE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // User agree the permission
                    startCaptureActivityForResult();
                } else {
                    // User disagree the permission
                    Toast.makeText(this, "You must agree the camera permission request before you use the code scan function", Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CaptureActivity.REQ_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        try {
                            String[] QRData = data.getStringExtra(CaptureActivity.EXTRA_SCAN_RESULT).split(";");
                            editDeviceUuid.setText(QRData[0]);
                            editDeviceName.setText(QRData[1]);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case RESULT_CANCELED:
                        if (data != null) {
                            ToastShowInBottom("Camera is not working correctly.");
                        }
                        break;
                }
                break;
        }
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

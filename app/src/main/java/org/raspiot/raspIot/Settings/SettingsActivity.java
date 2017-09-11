package org.raspiot.raspIot.Settings;

import android.content.Context;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;
import org.raspiot.raspIot.R;
import org.raspiot.raspIot.RaspApplication;
import org.raspiot.raspIot.UICommonOperations.ToastShow;
import org.raspiot.raspIot.databaseGlobal.HostAddrDB;
import org.raspiot.raspIot.jsonGlobal.ControlMessageJSON;
import org.raspiot.raspIot.networkGlobal.HttpUtil;
import org.raspiot.raspIot.networkGlobal.TCPClient;
import org.raspiot.raspIot.networkGlobal.ThreadCallbackListener;

import java.io.IOException;
import android.os.Handler;

import com.kyleduo.switchbutton.SwitchButton;

import okhttp3.Call;
import okhttp3.Response;

import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CLOUD_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CURRENT_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.DEFAULT_CLOUD_SERVER_ADDR;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.DEFAULT_RASP_SERVER_ADDR;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.RASP_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CurrentHostModeIsCloudServerMode;
import static org.raspiot.raspIot.jsonGlobal.JsonCommonOperations.buildJSON;

public class SettingsActivity extends AppCompatActivity {


    private static final int HOST_ERROR = -1;
    private static final int HOST_CONFIRM = 1;


    private String HostAddr;
    private Button confirmHostAddr;
    private EditText inputHostAddr;
    private SwitchButton switchServerMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initToolbar();
        initUIWidget();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return true;
    }

    //异步消息处理
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HOST_CONFIRM:   //service confirm
                    //获取消息 更新UI
                    // change UI here
                    Toast.makeText(SettingsActivity.this, "Host confirm", Toast.LENGTH_SHORT).show();

                    //更新服务器Addr
                    // update Server Addr
                    if(switchServerMode.isChecked()) {
                        saveHostAddrToDatabase(HostAddr, CLOUD_SERVER_ID);
                    }else {
                        saveHostAddrToDatabase(HostAddr, RASP_SERVER_ID);
                    }
                    updateCurrentServerAddrToDatabase();
                    inputHostAddr.clearFocus();
                    break;

                case HOST_ERROR:
                    ToastShow.ToastShowInCenter("This host couldn't provide service,\n please check!");
                    inputHostAddr.selectAll();
                    showSoftInputMethod(inputHostAddr);
                    break;

                default:
                    break;
            }
        }
    };





    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);//Add back icon
    }


    private void initUIWidget(){
        confirmHostAddr = (Button) findViewById(R.id.button_set_host);
        inputHostAddr = (EditText) findViewById(R.id.editText_input_host_addr) ;
        switchServerMode = (SwitchButton) findViewById(R.id.switch_server_mode);


        HostAddr = getHostAddrFromDatabase(CURRENT_SERVER_ID);
        inputHostAddr.setText(HostAddr);
        switchServerMode.setChecked(CurrentHostModeIsCloudServerMode());
        switchServerMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateCurrentServerAddrToDatabase();
                inputHostAddr.setText(HostAddr);
                /* Give a friendly message to the user */
                if(isChecked) {
                    inputHostAddr.setHint("Default:"+DEFAULT_CLOUD_SERVER_ADDR);
                    ToastShow.ToastShowInCenter("Please input Cloud server address");
                }else {
                    inputHostAddr.setHint("Default:"+DEFAULT_RASP_SERVER_ADDR);
                    ToastShow.ToastShowInCenter("Please input Rasp server address");
                }
                /* setFocus to EditText, enhance user experience */
                inputHostAddr.requestFocus();
                inputHostAddr.setFocusableInTouchMode(true);
                inputHostAddr.selectAll();
                showSoftInputMethod(inputHostAddr);
            }
        });

        confirmHostAddr.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                hideSoftInputMethod();
                    /*get host addr from inputHostAddr*/
                if(inputHostAddr.getText().toString().equals("")){
                    if(switchServerMode.isChecked()) {
                        HostAddr = DEFAULT_CLOUD_SERVER_ADDR;
                        inputHostAddr.setText(DEFAULT_CLOUD_SERVER_ADDR);
                    }else {
                        HostAddr = DEFAULT_RASP_SERVER_ADDR;
                        inputHostAddr.setText(DEFAULT_RASP_SERVER_ADDR);
                    }
                }else{
                    HostAddr = inputHostAddr.getText().toString();
                }
                /* check whether the host can provide service*/
                checkServicesWithNetwork();
            }
        });

        inputHostAddr.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER ) {
                    confirmHostAddr.performClick();   //模拟 press button_confirm
                    return true;  // if input enter ,  confirm
                }
                return false;
            }
        });

        final LinearLayout layoutAboutUs = (LinearLayout)findViewById(R.id.layout_about_us);
        TextView textTitleAboutUs = (TextView) findViewById(R.id.text_about_us);
        textTitleAboutUs.setOnClickListener(new View.OnClickListener(){
            boolean layoutIsChecked = false;
            @Override
            public void onClick(View v){
                if(layoutIsChecked = !layoutIsChecked)
                    layoutAboutUs.setVisibility(View.VISIBLE);
                else
                    layoutAboutUs.setVisibility(View.GONE);
            }
        });
    }


    private void checkServicesWithNetwork(){
        if(switchServerMode.isChecked()) {
            sendRequestWithOkHttp(HostAddr + "/api/checkServices");
        }else{
            ControlMessageJSON checkServicesJson = new ControlMessageJSON("get", "server", "checkServices");
            sendRequestWithSocket(HostAddr, buildJSON(checkServicesJson));
        }
    }




    private void updateCurrentServerAddrToDatabase(){
        if(switchServerMode.isChecked()){
            HostAddr = getHostAddrFromDatabase(CLOUD_SERVER_ID);
        }else {
            HostAddr = getHostAddrFromDatabase(RASP_SERVER_ID);
        }
        saveHostAddrToDatabase(HostAddr, CURRENT_SERVER_ID);
    }



    private void saveHostAddrToDatabase(String hostAddr, int serverId){
        HostAddrDB hostAddrDB = new HostAddrDB();
        hostAddrDB.setHostAddr(hostAddr);
        hostAddrDB.saveOrUpdate("id = ?", Integer.toString(serverId));
    }


    public static void showSoftInputMethod(View view){
        Context context = RaspApplication.getContext();
        ((InputMethodManager)(context.getSystemService(Context.INPUT_METHOD_SERVICE))).showSoftInput(view, 0);
    }

    private void hideSoftInputMethod(){
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }
    /****************************************************************************************************************/


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

    private void sendRequestWithOkHttp(String hostAddr){
        HttpUtil.sendOkHttpRequest(hostAddr, new okhttp3.Callback(){
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
        if(response.equals("Server is ready")){
            message.what = HOST_CONFIRM;
            handler.sendMessage(message);
        }else{
            onNetworkError();
        }
    }

    private void onNetworkError(){
        Message message = new Message();
        message.what = HOST_ERROR;
        handler.sendMessage(message);
    }
}

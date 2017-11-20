package org.raspiot.raspiot.Settings;

import android.content.Context;
import android.content.Intent;
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

import org.raspiot.raspiot.Auth.LogInActivity;
import org.raspiot.raspiot.Home.HomeActivity;
import org.raspiot.raspiot.R;
import org.raspiot.raspiot.DatabaseGlobal.HostAddrDB;
import org.raspiot.raspiot.JsonGlobal.ControlMessage;
import org.raspiot.raspiot.NetworkGlobal.HttpUtil;
import org.raspiot.raspiot.NetworkGlobal.TCPClient;
import org.raspiot.raspiot.NetworkGlobal.ThreadCallbackListener;

import java.io.IOException;
import android.os.Handler;

import com.kyleduo.switchbutton.SwitchButton;

import okhttp3.Call;
import okhttp3.Response;

import static org.raspiot.raspiot.Auth.LocalValidation.isLogInNeed;
import static org.raspiot.raspiot.UICommonOperations.KeyboardAction.showKeyboard;
import static org.raspiot.raspiot.UICommonOperations.ReminderShow.ToastShowInBottom;
import static org.raspiot.raspiot.UICommonOperations.ReminderShow.ToastShowInCenter;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.CLOUD_SERVER_ID;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.CURRENT_SERVER_ID;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.DEFAULT_CLOUD_SERVER_ADDR;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.DEFAULT_RASP_SERVER_ADDR;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.RASP_SERVER_ID;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.CurrentHostModeIsCloudServerMode;
import static org.raspiot.raspiot.JsonGlobal.JsonCommonOperations.buildJSON;

public class SettingsActivity extends AppCompatActivity {

    private static final int HOST_ERROR = -1;
    private static final int HOST_CONFIRM = 1;

    private String HostAddr;
    private Button confirmHostAddr;
    private EditText inputHostAddr;
    private TextView cloudServerExplain;
    private SwitchButton switchServerMode;
    private String dataFromNetworkResponse;

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
                HomeOrLogIn();
                finish();
                break;
            default:
        }
        return true;
    }

    //异步消息处理
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HOST_CONFIRM:   //service confirm
                    //获取消息 更新UI
                    // change UI here
                    ToastShowInBottom("Host confirm.\n" + dataFromNetworkResponse);

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
                    ToastShowInCenter(dataFromNetworkResponse);
                    inputHostAddr.selectAll();
                    showKeyboard(inputHostAddr);
                    break;

                default:
                    break;
            }
        }
    };





    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);//Add back icon
    }


    private void initUIWidget(){
        confirmHostAddr = (Button) findViewById(R.id.button_set_host);
        inputHostAddr = (EditText) findViewById(R.id.editText_input_host_addr) ;
        switchServerMode = (SwitchButton) findViewById(R.id.switch_server_mode);
        cloudServerExplain = (TextView) findViewById(R.id.text_cloud_mode_explain);

        HostAddr = getHostAddrFromDatabase(CURRENT_SERVER_ID);
        inputHostAddr.setText(HostAddr);
        currentModeExplain();
        switchServerMode.setChecked(CurrentHostModeIsCloudServerMode());
        switchServerMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateCurrentServerAddrToDatabase();
                inputHostAddr.setText(HostAddr);
                currentModeExplain();
                /* Give a friendly message to the user */
                if(isChecked) {
                    inputHostAddr.setHint("Default:"+DEFAULT_CLOUD_SERVER_ADDR);
                    ToastShowInCenter("Please input Cloud server address");
                }else {
                    inputHostAddr.setHint("Default:"+DEFAULT_RASP_SERVER_ADDR);
                    ToastShowInCenter("Please input Rasp server address");
                }
                /* setFocus to EditText, enhance user experience */
                inputHostAddr.requestFocus();
                inputHostAddr.setFocusableInTouchMode(true);
                inputHostAddr.selectAll();
                showKeyboard(inputHostAddr);
            }
        });

        confirmHostAddr.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                hideKeyboard();
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
                    return true;  // if input enter, confirm
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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            HomeOrLogIn();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void HomeOrLogIn(){
        if(isLogInNeed()){
            Intent intent_login= new Intent(SettingsActivity.this, LogInActivity.class);
            startActivity(intent_login);
        }else {
            Intent intent_home= new Intent(SettingsActivity.this, HomeActivity.class);
            startActivity(intent_home);
        }
    }

    private void checkServicesWithNetwork(){
        ControlMessage checkServices = new ControlMessage("get", "server", "checkServices");
        String checkServicesJson = buildJSON(checkServices);
        if(switchServerMode.isChecked()) {
            sendRequestWithOkHttp(HostAddr + "/api/relay", checkServicesJson);
        }else{
            sendRequestWithSocket(HostAddr, checkServicesJson);
        }
    }

    private void currentModeExplain(){
        if(switchServerMode.isChecked()){
            cloudServerExplain.setText("Cloud server mode: yours or raspiot cloud server,\nBut you need to log in if using raspiot cloud server.");
        }else {
            cloudServerExplain.setText("Local area network mode");
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


    private void hideKeyboard(){
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
            dataFromNetworkResponse = response;
            if (response.equals("raspServer is ready."))
                message.what = HOST_CONFIRM;
            else if(response.equals("raspServer is offline."))
                message.what = HOST_CONFIRM;
            else if(response.equals("You need to log in."))
                message.what = HOST_CONFIRM;
            handler.sendMessage(message);
        }
        else
            onNetworkError();
    }

    private void onNetworkError(){
        dataFromNetworkResponse = "This host couldn't provide service,\nplease check!";
        Message message = new Message();
        message.what = HOST_ERROR;
        handler.sendMessage(message);
    }
}

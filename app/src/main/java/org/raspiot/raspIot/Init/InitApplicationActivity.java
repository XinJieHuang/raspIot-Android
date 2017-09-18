package org.raspiot.raspIot.Init;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.raspiot.raspIot.Auth.LogInActivity;
import org.raspiot.raspIot.Home.FullscreenActivity;
import org.raspiot.raspIot.Home.HomeActivity;
import org.raspiot.raspIot.R;
import org.raspiot.raspIot.databaseGlobal.UserInfoDB;

import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CLOUD_SERVER_ID;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.CurrentHostModeIsCloudServerMode;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.DEFAULT_CLOUD_SERVER_ADDR;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.getCurrentUserInfo;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;
import static org.raspiot.raspIot.databaseGlobal.DatabaseCommonOperations.initHostAddrDatabase;

public class InitApplicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initApp();
    }

    private void initApp(){
        initHostAddrDatabase();
        UserInfoDB userInfo = getCurrentUserInfo();
        if(userInfo == null && CurrentHostModeIsCloudServerMode() && getHostAddrFromDatabase(CLOUD_SERVER_ID).equals(DEFAULT_CLOUD_SERVER_ADDR)){
                Intent intent_login= new Intent(InitApplicationActivity.this, LogInActivity.class);
                startActivity(intent_login);
        }else {
            Intent intent_home= new Intent(InitApplicationActivity.this, HomeActivity.class);
            startActivity(intent_home);
        }
        /*App start page*/
        startPage();
        finish();
    }

    private void startPage(){
        Intent intent_welcome = new Intent(InitApplicationActivity.this,FullscreenActivity.class);
        startActivity(intent_welcome);
    }
}

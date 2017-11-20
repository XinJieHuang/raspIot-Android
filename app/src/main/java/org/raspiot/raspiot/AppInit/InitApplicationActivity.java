package org.raspiot.raspiot.AppInit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.raspiot.raspiot.Auth.LogInActivity;
import org.raspiot.raspiot.Home.HomeActivity;

import static org.raspiot.raspiot.Auth.LocalValidation.isLogInNeed;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.initHostAddrDatabase;

public class InitApplicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initApp();
    }

    private void initApp(){
        initHostAddrDatabase();
        HomeOrLogIn();
        /*App start page*/
        startPage();
        finish();
    }


    private void HomeOrLogIn(){
        if(isLogInNeed()){
            Intent intent_login= new Intent(InitApplicationActivity.this, LogInActivity.class);
            startActivity(intent_login);
        }else {
            Intent intent_home= new Intent(InitApplicationActivity.this, HomeActivity.class);
            startActivity(intent_home);
        }
    }

    private void startPage(){
        Intent intent_welcome = new Intent(InitApplicationActivity.this,FullscreenActivity.class);
        startActivity(intent_welcome);
    }
}

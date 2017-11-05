package org.raspiot.raspiot.Auth;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.raspiot.raspiot.Auth.json.LoginForm;
import org.raspiot.raspiot.Home.HomeActivity;
import org.raspiot.raspiot.R;
import org.raspiot.raspiot.Settings.SettingsActivity;
import org.raspiot.raspiot.DatabaseGlobal.UserInfoDB;
import org.raspiot.raspiot.NetworkGlobal.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

import static org.raspiot.raspiot.Auth.LocalValidation.isEmailValid;
import static org.raspiot.raspiot.Auth.LocalValidation.isPasswordValid;
import static org.raspiot.raspiot.UICommonOperations.KeyboardAction.showKeyboard;
import static org.raspiot.raspiot.UICommonOperations.ReminderShow.ToastShowInBottom;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.DEFAULT_CLOUD_SERVER_ADDR;
import static org.raspiot.raspiot.JsonGlobal.JsonCommonOperations.buildJSON;

/**
 * A login screen that offers login via email/password.
 */
public class LogInActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private String dataFromNetworkResponse;

    private LoginForm loginForm;
    private Button logIn;

    private static final int LOG_IN_SUCCEED = 1;
    private static final int LOG_IN_ERROR = -1;
    private static final int NETWORK_ERROR = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        initUIWidget();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case LOG_IN_SUCCEED:
                    saveUserInfoToDatabase();
                    Intent intent_home= new Intent(LogInActivity.this, HomeActivity.class);
                    startActivity(intent_home);
                    finish();
                    break;

                case LOG_IN_ERROR:
                    ToastShowInBottom(dataFromNetworkResponse);
                    break;

                case NETWORK_ERROR:
                    ToastShowInBottom("Network error");
                    break;

                default:
                    break;
            }
        }
    };

    private void initUIWidget(){
        Button settings = (Button) findViewById(R.id.log_in_settings_button);
        settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_settings= new Intent(LogInActivity.this, SettingsActivity.class);
                startActivity(intent_settings);
                finish();
            }
        });

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.login_email);
        mPasswordView = (EditText) findViewById(R.id.log_in_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    logIn.performClick();
                    return true;
                }
                return false;
            }
        });

        logIn = (Button) findViewById(R.id.log_in_button);
        logIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                String email = getEmail();
                String password = getPassword();
                if(!isEmailValid(email)) {
                    mEmailView.setError("Invalid E-mail address!");
                    mEmailView.requestFocus();
                    mEmailView.setFocusableInTouchMode(true);
                    showKeyboard(mEmailView);
                }else if(!isPasswordValid(password)){
                    mPasswordView.setError("Password's length is invalid!");
                    mPasswordView.requestFocus();
                    mPasswordView.setFocusableInTouchMode(true);
                    showKeyboard(mPasswordView);
                }else
                    attemptLogin();
            }
        });

        Button toSignUp = (Button)findViewById(R.id.to_sign_up_button);
        toSignUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSignUp= new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intentSignUp);
            }
        });
        mProgressView = findViewById(R.id.login_progress);
    }


    private void saveUserInfoToDatabase(){
        UserInfoDB userInfo = new UserInfoDB();
        userInfo.setEmail(getEmail());
        userInfo.setUsername("admin");
        userInfo.setAuthStatus(true);
        userInfo.saveOrUpdate("email = ?", getEmail());
    }

    private String getEmail(){
        return mEmailView.getText().toString();
    }

    private String getPassword(){
        return mPasswordView.getText().toString();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private boolean attemptLogin(){
        String email = getEmail();
        String password = getPassword();
        loginForm = new LoginForm(email, password);
        String loginFormJson = buildJSON(loginForm);
        attemptLoginWithOkHttp(DEFAULT_CLOUD_SERVER_ADDR + "/api/login", loginFormJson);
        return true;
    }

    private void attemptLoginWithOkHttp(String address, String data){
        HttpUtil.sendOkHttpRequest(address, data, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException{
               onNetworkResponse(response.body().string());
            }
            @Override
            public void onFailure(Call call, IOException E){
                onNetworkError();
            }
        });
    }

    private void onNetworkResponse(String response){
        Message message = new Message();
        if(!response.equals("")) {
            if(response.equals("Authentication success."))
                message.what = LOG_IN_SUCCEED;
            else if(response.contains("Invalid")) {
                dataFromNetworkResponse = response;
                message.what = LOG_IN_ERROR;
            }
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


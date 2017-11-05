package org.raspiot.raspiot.Auth;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.raspiot.raspiot.Auth.json.SignUpForm;
import org.raspiot.raspiot.R;
import org.raspiot.raspiot.NetworkGlobal.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

import static org.raspiot.raspiot.Auth.LocalValidation.isEmailValid;
import static org.raspiot.raspiot.Auth.LocalValidation.isPasswordEqual;
import static org.raspiot.raspiot.Auth.LocalValidation.isPasswordValid;
import static org.raspiot.raspiot.Auth.LocalValidation.isUsernameValid;
import static org.raspiot.raspiot.UICommonOperations.KeyboardAction.showKeyboard;
import static org.raspiot.raspiot.UICommonOperations.ReminderShow.ToastShowInBottom;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.DEFAULT_CLOUD_SERVER_ADDR;
import static org.raspiot.raspiot.JsonGlobal.JsonCommonOperations.buildJSON;

public class SignUpActivity extends AppCompatActivity {
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private View mProgressView;
    private String dataFromNetworkResponse;

    private SignUpForm signUpForm;
    private Button signUp;

    private static final int SIGN_UP_SUCCEED = 1;
    private static final int SIGN_UP_ERROR = -1;
    private static final int NETWORK_ERROR = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initUIWidget();
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SIGN_UP_SUCCEED:
                    ToastShowInBottom(dataFromNetworkResponse + "\nYou can now login.");
                    finish();
                    break;

                case SIGN_UP_ERROR:
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
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.sign_up_email);

        mUsernameView = (EditText) findViewById(R.id.sign_up_username);

        mPasswordView = (EditText) findViewById(R.id.sign_up_password);

        mConfirmPasswordView = (EditText) findViewById(R.id.sign_up_password_confirm);
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    signUp.performClick();
                    return true;
                }
                return false;
            }
        });

        signUp = (Button) findViewById(R.id.sign_up_button);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                String email = getEmail();
                String username = getUsername();
                String password = getPassword();
                String confirmPassword = getConfirmPassword();
                if(!isEmailValid(email)) {
                    mEmailView.setError("Invalid E-mail address!");
                    mEmailView.requestFocus();
                    mEmailView.setFocusableInTouchMode(true);
                    showKeyboard(mEmailView);
                }
                else if(!isUsernameValid(username)){
                    mUsernameView.setError("Username's length is invalid!");
                    mUsernameView.requestFocus();
                    mUsernameView.setFocusableInTouchMode(true);
                    showKeyboard(mUsernameView);
                }
                else if(!isPasswordValid(password)){
                    mPasswordView.setError("Password's length is invalid!");
                    mPasswordView.requestFocus();
                    mPasswordView.setFocusableInTouchMode(true);
                    showKeyboard(mPasswordView);
                }
                else if(!isPasswordEqual(password, confirmPassword)){
                    mConfirmPasswordView.setError("Password is not equal!");
                    mConfirmPasswordView.requestFocus();
                    mConfirmPasswordView.setFocusableInTouchMode(true);
                    showKeyboard(mConfirmPasswordView);
                }
                else
                    attemptSignUp();
            }
        });

        Button toLogIn = (Button)findViewById(R.id.to_log_in_button);
        toLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mProgressView = findViewById(R.id.login_progress);
    }

    private String getEmail(){
        return mEmailView.getText().toString();
    }

    private String getUsername() {
        return mUsernameView.getText().toString();
    }

    private String getPassword(){
        return mPasswordView.getText().toString();
    }

    private String getConfirmPassword() {
        return mConfirmPasswordView.getText().toString();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private boolean attemptSignUp(){
        String email = getEmail();
        String username = getUsername();
        String password = getPassword();
        signUpForm = new SignUpForm(email, username, password);
        String signUpFormJson = buildJSON(signUpForm);
        attemptSignUpWithOkHttp(DEFAULT_CLOUD_SERVER_ADDR + "/api/signup", signUpFormJson);
        return true;
    }

    private void attemptSignUpWithOkHttp(String address, String data){
        HttpUtil.sendOkHttpRequest(address, data, new okhttp3.Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
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
            dataFromNetworkResponse = response;
            if(response.equals("Registration completed."))
                message.what = SIGN_UP_SUCCEED;
            else if(response.contains("Please replace it.")) {
                message.what = SIGN_UP_ERROR;
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

package org.raspiot.raspot.UICommonOperations;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.raspiot.raspot.RaspApplication;

/**
 * Created by asus on 2017/9/19.
 */

public class KeyboardAction {
    public static void showKeyboard(View view){
        Context context = RaspApplication.getContext();
        ((InputMethodManager)(context.getSystemService(Context.INPUT_METHOD_SERVICE))).showSoftInput(view, 0);
    }
}

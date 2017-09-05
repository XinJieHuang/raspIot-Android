package org.raspiot.raspIot.UICommonOperations;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import org.raspiot.raspIot.RaspApplication;

/**
 * Created by asus on 2017/8/25.
 */


public class ToastShow {
    private static Toast toast = null;

    public static void ToastShowInCenter(String text) {
        if(toast == null) {
            toast = Toast.makeText(RaspApplication.getContext(), text, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
        }else {
            toast.setText(text);
        }
        toast.show();
        toast = null;
    }

    public static void ToastShowInBottom(String text) {
        if(toast == null) {
            toast = Toast.makeText(RaspApplication.getContext(), text, Toast.LENGTH_SHORT);
            //The default is displayed at the bottom
        }else {
            toast.setText(text);
        }
        toast.show();
        toast = null;
    }
}

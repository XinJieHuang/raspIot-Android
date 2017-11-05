package org.raspiot.raspiot.UICommonOperations;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.Toast;

import static org.raspiot.raspiot.RaspApplication.getContext;

/**
 * Created by asus on 2017/8/25.
 */


public class ReminderShow {
    private static Toast toast = null;

    public static void ToastShowInCenter(String text) {
        if(toast == null) {
            toast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
        }else {
            toast.setText(text);
        }
        toast.show();
        toast = null;
    }

    public static void ToastShowInBottom(String text) {
        if(toast == null) {
            toast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
            //The default is displayed at the bottom
        }else {
            toast.setText(text);
        }
        toast.show();
        toast = null;
    }


    public static void showWarning(Context context, String msg){
        AlertDialog.Builder warning = new AlertDialog.Builder(context);
        warning.setCancelable(false);
        warning.setTitle("Warning");
        warning.setMessage(msg);
        warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        warning.show();
    }

}

package org.raspiot.raspiot;

import android.content.Context;

import org.litepal.LitePalApplication;

/**
 * Created by asus on 2017/8/21.
 */

public class RaspApplication extends LitePalApplication {
    private static Context context;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }
}

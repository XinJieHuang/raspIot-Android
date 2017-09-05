package org.raspiot.raspIot.networkGlobal;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by asus on 2017/6/4.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://" + address)
                            .build();
                    client.newCall(request).enqueue(callback);
            }
}
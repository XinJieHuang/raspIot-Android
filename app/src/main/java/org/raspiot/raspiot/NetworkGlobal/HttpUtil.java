package org.raspiot.raspot.NetworkGlobal;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by asus on 2017/6/4.
 */

public class HttpUtil {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static void sendOkHttpRequest(String address, String cmdJson, okhttp3.Callback callback) {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(JSON, cmdJson);
                    Request request = new Request.Builder()
                            .url("http://" + address)
                            .post(body)
                            .build();
                    client.newCall(request).enqueue(callback);
            }
}
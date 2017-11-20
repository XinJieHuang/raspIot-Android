package org.raspiot.raspot.NetworkGlobal;

/**
 * Created by asus on 2017/8/25.
 */

public interface ThreadCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}

package org.raspiot.raspiot.NetworkGlobal;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by asus on 2017/8/24.
 */

public class TCPClient {
    public static void tcpClient(final String ip, final int port, final String data, final ThreadCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    //Socket socket = new Socket(ip, port);
                    Socket socket = new Socket();
                    SocketAddress address = new InetSocketAddress(ip, port);
                    socket.connect(address, 3000);    // timeout: 3 seconds

                    OutputStream out = socket.getOutputStream();
                    InputStream in = socket.getInputStream();
                    out.write(data.getBytes());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    /* *****************put in response************ */
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null)
                        response.append(line);
                    if(listener != null)
                        listener.onFinish(response.toString());
                    Log.d("Thread: ", response.toString());
                    /* ******************************************* */
                    out.close();
                    in.close();
                    socket.close();
                }catch (Exception e){
                    if(listener != null)
                        listener.onError(e);
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

package org.raspiot.raspot.JsonGlobal;

import com.google.gson.Gson;

/**
 * Created by asus on 2017/8/25.
 */

public class JsonCommonOperations {

    public static String buildJSON(Object o){
        Gson gson = new Gson();
        try{
            return gson.toJson(o);
        }catch (Exception e){
            return null;
        }

    }
}

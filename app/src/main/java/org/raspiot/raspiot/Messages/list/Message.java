package org.raspiot.raspiot.Messages.list;

/**
 * Created by asus on 2018/1/12.
 */

public class Message {
    private int imageId;
    private String msg;

    public Message(int imageId, String msg) {
        this.imageId = imageId;
        this.msg = msg;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

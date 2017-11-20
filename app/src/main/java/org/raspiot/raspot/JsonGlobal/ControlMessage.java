package org.raspiot.raspot.JsonGlobal;

import static org.raspiot.raspot.DatabaseGlobal.DatabaseCommonOperations.getCurrentUserInfo;

/**
 * Created by asus on 2017/8/24.
 */

public class ControlMessage {
    private String cmd;
    private String target;
    private String value;
    private String identity;
    private String updateTime;

    public ControlMessage(String cmd, String target, String value, String updateTime) {
        this.cmd = cmd;
        this.target = target;
        this.value = value;
        this.identity = getCurrentUserInfo().getEmail();
        this.updateTime = updateTime;
    }

    public ControlMessage(String cmd, String target, String value) {
        this.cmd = cmd;
        this.target = target;
        this.value = value;
        this.identity = getCurrentUserInfo().getEmail();
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}

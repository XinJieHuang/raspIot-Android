package org.raspiot.raspiot.Room.list;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.raspiot.raspiot.DatabaseGlobal.DeviceDB;
import org.raspiot.raspiot.JsonGlobal.ControlMessage;
import org.raspiot.raspiot.NetworkGlobal.TCPClient;
import org.raspiot.raspiot.NetworkGlobal.ThreadCallbackListener;
import org.raspiot.raspiot.R;
import org.raspiot.raspiot.UICommonOperations.DensityUtil;

import java.util.List;

import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.UNAUTHORIZED_DEVICES;
import static org.raspiot.raspiot.Home.RoomDatabaseHandler.getRestRoomList;
import static org.raspiot.raspiot.Room.DeviceDatabaseHandler.moveDeviceInDatabase;
import static org.raspiot.raspiot.Room.RoomActivity.roomName;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.CURRENT_SERVER_ID;
import static org.raspiot.raspiot.DatabaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;
import static org.raspiot.raspiot.JsonGlobal.JsonCommonOperations.buildJSON;
import static org.raspiot.raspiot.UICommonOperations.ReminderShow.ToastShowInBottom;

/**
 * Created by asus on 2017/9/9.
 */

class DeviceListHandler {
    private final static int CMD_SUCCEED = 1;
    private final static int CMD_FAILED = -1;
    static void setValueToDeviceContent(String deviceName, String deviceContentName, final DeviceContent deviceContent, final String newValue){
        String target = "deviceContent:" + roomName + "/" + deviceName + "/" + deviceContentName;
        ControlMessage setDeviceContentToNewValue = new ControlMessage("set", target, newValue);
        String setValueCmd = buildJSON(setDeviceContentToNewValue);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CMD_SUCCEED:
                        deviceContent.setValue(newValue);
                        break;
                    case CMD_FAILED:
                        ToastShowInBottom("Something error.\nSet new value failed.");
                        break;
                    default:
                        break;
                }
            }
        };
        sendCmd(setValueCmd, handler);
    }


    static void showBottomDialog(final Context context, final int position, final List<Device> deviceList, final DeviceAdapter adapter){
        final Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.device_bottom_dialog_content, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
        params.width = context.getResources().getDisplayMetrics().widthPixels - DensityUtil.dp2px(context, 16f);
        params.bottomMargin = DensityUtil.dp2px(context, 8f);
        contentView.setLayoutParams(params);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.setCanceledOnTouchOutside(true);

        TextView move = (TextView) contentView.findViewById(R.id.device_move);
        TextView rename = (TextView) contentView.findViewById(R.id.device_rename);
        TextView delete = (TextView) contentView.findViewById(R.id.device_delete);
        TextView cancel = (TextView) contentView.findViewById(R.id.device_bottom_dialog_cancel);

        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
                showMoveDeviceDialog(context, position, deviceList, adapter);
            }
        });
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
                showRenameDialog(context, position, deviceList, adapter);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
                /*if (removeRoom(mRoomList.get(position).getName())) {
                    deleteRoomFromDatabase(mRoomList.get(position).getName());
                    mRoomList.remove(position);
                    notifyItemRemoved(position);
                }*/
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
            }
        });
        bottomDialog.show();
    }

    private static void showRenameDialog(final Context context, final int position, final List<Device> deviceList, final DeviceAdapter adapter){
        AlertDialog.Builder renameDeviceDialog = new AlertDialog.Builder(context);

        final String oldName = deviceList.get(position).getGroupItem().getName();
        final EditText newDeviceName = new EditText(context);
        newDeviceName.setHint("Input new device name");
        newDeviceName.setText(oldName);
        newDeviceName.selectAll();
        newDeviceName.setMaxLines(1);
        newDeviceName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    return true;  // if input Enter, put away
                }
                return false;
            }
        });
        renameDeviceDialog.setCancelable(false);
        renameDeviceDialog.setTitle("Rename " + oldName);
        renameDeviceDialog.setView(newDeviceName);
        renameDeviceDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (newDeviceName.getText().toString().isEmpty()) {
                    newDeviceName.setText(oldName);
                }else {
                    final String newName = newDeviceName.getText().toString();
                    ControlMessage renameDeviceCmd = new ControlMessage("set", "device:" + roomName + "/" + oldName, roomName + "/" + newName);
                    String renameDeviceJson = buildJSON(renameDeviceCmd);

                    final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case CMD_SUCCEED:
                                 /*update list*/
                                    deviceList.get(position).getGroupItem().setName(newName);
                                    adapter.notifyItemChanged(position);
                                 /* update database*/
                                    DeviceDB deviceDB = new DeviceDB();
                                    deviceDB.setName(newName);
                                    deviceDB.updateAll("name = ?", oldName);
                                    break;
                                case CMD_FAILED:
                                    break;
                                default:
                                    break;
                            }
                        }
                    };
                    sendCmd(renameDeviceJson, handler);
                }
            }
        });
        renameDeviceDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        renameDeviceDialog.show();
    }


    private static void showMoveDeviceDialog(final Context context, final int position, final List<Device> deviceList, final DeviceAdapter adapter){
        final AlertDialog.Builder moveDeviceDialog = new AlertDialog.Builder(context);

        List<String> roomList = getRestRoomList(roomName, UNAUTHORIZED_DEVICES);
        final String[] roomItemList = roomList.toArray(new String[roomList.size()]);
        final String deviceName = deviceList.get(position).getGroupItem().getName();

        moveDeviceDialog.setCancelable(false);
        moveDeviceDialog.setTitle("Move [" + deviceName + "] to");
        moveDeviceDialog.setItems(roomItemList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                ControlMessage moveDeviceCmd = new ControlMessage("set", "device:" + roomName + "/" + deviceName, roomItemList[which] + "/" + deviceName);
                String moveDeviceJson = buildJSON(moveDeviceCmd);
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case CMD_SUCCEED:
                                ToastShowInBottom("Move to " + roomItemList[which]);
                                moveDeviceInDatabase(roomName, roomItemList[which], deviceName);
                                deviceList.remove(position);
                                adapter.notifyItemRemoved(position);
                                break;
                            case CMD_FAILED:
                                break;
                            default:
                                break;
                        }
                    }
                };
                sendCmd(moveDeviceJson, handler);
            }
        });
        moveDeviceDialog.show();
    }

    private static void sendCmd(String data, final Handler handler){
        String addr = getHostAddrFromDatabase(CURRENT_SERVER_ID);
        String ip = addr.split(":")[0];
        int port = Integer.parseInt(addr.split(":")[1]);

        TCPClient.tcpClient(ip, port, data, new ThreadCallbackListener(){
            Message msg = new Message();
            @Override
            public void onFinish(String response) {
                msg.what = CMD_SUCCEED;
                handler.sendMessage(msg);
            }
            @Override
            public void onError(Exception e) {
                msg.what = CMD_FAILED;
                handler.sendMessage(msg);
                e.printStackTrace();
            }
        });
    }
}

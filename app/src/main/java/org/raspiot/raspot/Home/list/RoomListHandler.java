package org.raspiot.raspot.Home.list;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.litepal.crud.DataSupport;
import org.raspiot.raspot.R;
import org.raspiot.raspot.DatabaseGlobal.RoomDB;
import org.raspiot.raspot.JsonGlobal.ControlMessage;
import org.raspiot.raspot.NetworkGlobal.TCPClient;
import org.raspiot.raspot.NetworkGlobal.ThreadCallbackListener;
import org.raspiot.raspot.Room.RoomActivity;
import org.raspiot.raspot.UICommonOperations.DensityUtil;

import java.util.List;

import static org.raspiot.raspot.DatabaseGlobal.DatabaseCommonOperations.CURRENT_SERVER_ID;
import static org.raspiot.raspot.DatabaseGlobal.DatabaseCommonOperations.UNAUTHORIZED_DEVICES;
import static org.raspiot.raspot.DatabaseGlobal.DatabaseCommonOperations.getHostAddrFromDatabase;
import static org.raspiot.raspot.Home.HomeActivity.ROOM_NAME;
import static org.raspiot.raspot.Home.HomeDatabaseHandler.deleteRoomFromDatabase;
import static org.raspiot.raspot.JsonGlobal.JsonCommonOperations.buildJSON;
import static org.raspiot.raspot.RaspApplication.getContext;
import static org.raspiot.raspot.UICommonOperations.ReminderShow.showWarning;

/**
 * Created by asus on 2017/8/27.
 */

public class RoomListHandler {

    private static boolean TrueOrFalse = true;

    /*do not use adapter.notifyDataSetChanged(); in every way I can*/
    public static void updateRoomListAndNotifyItem(List<RoomDB> roomDBList, List<Room> roomList, RoomAdapter adapter){
        if(roomDBList == null)
            return;

        /*length = max(roomList.size(), roomJSONList.size())*/
        for(int i = 0, length = roomList.size() > roomDBList.size() ? roomList.size() : roomDBList.size();i < length; i++) {

            /*Means roomJSONList.size() > roomList.size()*/
            if(i == roomList.size()){
                for(int j = i; j < length; j++)
                    roomList.add(new Room(roomDBList.get(j).getName(), R.drawable.recyclerview_item_image));
                /*Add elements*/
                adapter.notifyItemRangeInserted(i, roomList.size() - i);
                /*i still small than length, but roomList.size() == length*/
                break;
            }

            /*Means roomList.size() > roomJSONList.size()*/
            else if(i == roomDBList.size()){
                int j;
                /*roomList.size() is reducing*/
                for(j = i; i < roomList.size(); j++)
                    roomList.remove(i);
                /*Remove elements*/
                adapter.notifyItemRangeRemoved(i, j);
                /*i still small than length, but max(roomList.size(), roomJSONList.size()) small than length at this time*/
                break;
            }

            /* Only when i < min(roomList.size(), roomJSONList.size()) */
            else {
                Room room = new Room(roomDBList.get(i).getName(), R.drawable.recyclerview_item_image);
                if (roomList.get(i).equals(room))
                    continue;
                else {
                    roomList.set(i, room);
                    adapter.notifyItemChanged(i);
                }
            }
        }
    }

    public static void getIntoRoom(String roomName){
        Context context = getContext();
        Intent intent = new Intent(context, RoomActivity.class);
        intent.putExtra(ROOM_NAME, roomName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    protected static void showBottomDialog(final Context context, final int position, final List<Room> roomList, final RoomAdapter adapter){
        final String roomName = roomList.get(position).getName();
        final Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.room_bottom_dialog_content, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
        params.width = context.getResources().getDisplayMetrics().widthPixels - DensityUtil.dp2px(context, 16f);
        params.bottomMargin = DensityUtil.dp2px(context, 8f);
        contentView.setLayoutParams(params);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.setCanceledOnTouchOutside(true);

        TextView rename = (TextView) contentView.findViewById(R.id.room_rename);
        TextView delete = (TextView) contentView.findViewById(R.id.room_delete);
        TextView cancel = (TextView) contentView.findViewById(R.id.room_bottom_dialog_cancel);
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
                if(roomName.equals(UNAUTHORIZED_DEVICES))
                    showWarning(context, UNAUTHORIZED_DEVICES + " can't be renamed.You can close it in Settings.");
                else
                    showRenameDialog(context, position, roomList, adapter);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
                if(roomName.equals(UNAUTHORIZED_DEVICES))
                    showWarning(context, UNAUTHORIZED_DEVICES + " can't be deleted. You can close it in Settings.");
                else
                    showDelRoomDialog(context, position, roomList, adapter);
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

    private static void showRenameDialog(final Context context,final int position, final List<Room> roomList, final RoomAdapter adapter){
        AlertDialog.Builder renameRoomDialog = new AlertDialog.Builder(context);

        final String oldName = roomList.get(position).getName();
        final EditText newRoomName = new EditText(context);
        newRoomName.setHint("Input new room name");
        newRoomName.setText(oldName);
        newRoomName.selectAll();
        newRoomName.setMaxLines(1);
        newRoomName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    return true;  // if input Enter, put away
                }
                return false;
            }
        });
        renameRoomDialog.setCancelable(false);
        renameRoomDialog.setTitle("Rename " + oldName);
        renameRoomDialog.setView(newRoomName);
        renameRoomDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (newRoomName.getText().toString().isEmpty()) {
                    String roomName = "new room";
                    for (int i = 1; ; i++) {
                        if (DataSupport.where("name = ?", roomName).find(RoomDB.class).isEmpty()) {
                            newRoomName.setText(roomName);
                            break;
                        }
                        roomName = "new room " + i;
                    }
                }
                String newName = newRoomName.getText().toString();
                ControlMessage deleteRoomCmd = new ControlMessage("set", "room:" + oldName, newName);
                String deleteRoomJson = buildJSON(deleteRoomCmd);
                if(renameRoom(deleteRoomJson)){
                    /*update list*/
                    roomList.get(position).setName(newName);
                    adapter.notifyItemChanged(position);
                    /* update database*/
                    RoomDB roomDB = new RoomDB();
                    roomDB.setName(newName);
                    roomDB.updateAll("name = ?", oldName);
                }
            }
        });
        renameRoomDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        renameRoomDialog.show();
    }

    private static boolean renameRoom(String data){
        String addr = getHostAddrFromDatabase(CURRENT_SERVER_ID);
        String ip = addr.split(":")[0];
        int port = Integer.parseInt(addr.split(":")[1]);
        TCPClient.tcpClient(ip, port, data, new ThreadCallbackListener() {
            @Override
            public void onFinish(String response) {
                TrueOrFalse = true;
            }
            @Override
            public void onError(Exception e) {
                TrueOrFalse = false;
                e.printStackTrace();
            }
        });
        return TrueOrFalse;
    }

    private static void showDelRoomDialog(final Context context,final int position, final List<Room> roomList, final RoomAdapter adapter){
        AlertDialog.Builder delRoom = new AlertDialog.Builder(context);
        final String roomName = roomList.get(position).getName();
        delRoom.setCancelable(false);
        delRoom.setTitle("Delete " + roomName);
        delRoom.setMessage("All devices of " + roomName + " will be moved to " + UNAUTHORIZED_DEVICES + ".");
        delRoom.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ControlMessage deleteRoomCmd = new ControlMessage("del", "room", roomName);
                String data = buildJSON(deleteRoomCmd);
                if (deleteRoom(data)) {
                    deleteRoomFromDatabase(roomList.get(position).getName());
                    roomList.remove(position);
                    adapter.notifyItemRemoved(position);
                }
            }
        });
        delRoom.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        delRoom.show();
    }

    private static boolean deleteRoom(String data){
        String addr = getHostAddrFromDatabase(CURRENT_SERVER_ID);
        String ip = addr.split(":")[0];
        int port = Integer.parseInt(addr.split(":")[1]);

        TCPClient.tcpClient(ip, port, data, new ThreadCallbackListener(){
            @Override
            public void onFinish(String response) {
                TrueOrFalse = true;
            }
            @Override
            public void onError(Exception e) {
                TrueOrFalse = false;
                e.printStackTrace();
            }
        });
        return TrueOrFalse;
    }
}

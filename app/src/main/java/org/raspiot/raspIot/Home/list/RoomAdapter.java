package org.raspiot.raspIot.Home.list;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.raspiot.raspIot.Home.HomeActivity;
import org.raspiot.raspIot.R;
import org.raspiot.raspIot.Room.RoomActivity;

import java.util.List;

import static org.raspiot.raspIot.Home.HomeActivity.ROOM_NAME;
import static org.raspiot.raspIot.Home.HomeDatabaseHandler.deleteRoomFromDatabase;
import static org.raspiot.raspIot.Home.list.HomeListHandler.canRemoveItemOrNot;


/**
 * Created by asus on 2017/5/20.
 */

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
    private Context mContext;
    private List<Room> mRoomList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View roomView;
        ImageView roomImage;
        TextView roomName;

        public ViewHolder(View view){
            super(view);
            roomView = view;
            roomImage = (ImageView)view.findViewById(R.id.room_image);
            roomName = (TextView)view.findViewById(R.id.room_name);
        }
    }

    public RoomAdapter(List<Room> roomList){
        mRoomList = roomList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.roomView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int position = holder.getAdapterPosition();
                Room room = mRoomList.get(position);
                Intent intent = new Intent(mContext, RoomActivity.class);
                intent.putExtra(ROOM_NAME, room.getName());
                mContext.startActivity(intent);
            }
        });
        holder.roomView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                if(canRemoveItemOrNot(mRoomList.get(position).getName())) {
                    deleteRoomFromDatabase(mRoomList.get(position).getName());
                    mRoomList.remove(position);
                    notifyItemRemoved(position);
                }
                return true;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        Room room = mRoomList.get(position);
        Glide.with(mContext).load(room.getImageId()).into(holder.roomImage);
        holder.roomName.setText(room.getName());
    }

    @Override
    public int getItemCount(){
        return mRoomList.size();
    }
}

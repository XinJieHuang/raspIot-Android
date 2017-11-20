package org.raspiot.raspiot.Room.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kyleduo.switchbutton.SwitchButton;

import org.raspiot.raspiot.R;

import java.util.ArrayList;
import java.util.List;

import static org.raspiot.raspiot.Room.list.DeviceListHandler.setValueToDeviceContent;
import static org.raspiot.raspiot.Room.list.DeviceListHandler.showBottomDialog;
import static org.raspiot.raspiot.UICommonOperations.ReminderShow.ToastShowInBottom;

/**
 * Created by asus on 2017/7/31.
 */

public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Device> mDeviceList;
    private List<Boolean> groupItemStatus;

    private static class GroupItemViewHolder extends RecyclerView.ViewHolder{
        ImageView deviceImage;
        TextView deviceName;
        TextView deviceStatus;

        private GroupItemViewHolder(View view){
            super(view);
            deviceImage = (ImageView) view.findViewById(R.id.device_image);
            deviceName = (TextView) view.findViewById(R.id.device_name);
            deviceStatus = (TextView) view.findViewById(R.id.device_status);
        }
    }

    private static class SubItemViewHolder extends RecyclerView.ViewHolder{
        LinearLayout deviceContentTextLayout;
        TextView deviceContentTextName;
        TextView deviceContentText;

        LinearLayout deviceContentImageLayout;
        TextView deviceContentImageName;
        ImageView deviceContentImage;

        LinearLayout deviceContentSwitchLayout;
        TextView deviceContentSwitchName;
        SwitchButton deviceContentSwitch;

        private SubItemViewHolder(View view){
            super(view);
            deviceContentTextLayout = (LinearLayout) view.findViewById(R.id.device_content_text_layout);
            deviceContentImageLayout = ( LinearLayout) view.findViewById(R.id.device_content_image_layout);
            deviceContentSwitchLayout = (LinearLayout) view.findViewById(R.id.device_content_switch_layout);

            deviceContentTextName = (TextView) view.findViewById(R.id.device_content_text_name);
            deviceContentText = (TextView) view.findViewById(R.id.device_content_text);
            deviceContentImageName = (TextView) view.findViewById(R.id.device_content_image_name);
            deviceContentImage = (ImageView) view.findViewById(R.id.device_content_image);
            deviceContentSwitchName = (TextView) view.findViewById(R.id.device_content_switch_name);
            deviceContentSwitch = (SwitchButton) view.findViewById(R.id.device_content_switch);
        }
    }

    private static class ItemStatus {
        private static final int VIEW_TYPE_GROUPITEM = 0;
        private static final int VIEW_TYPE_SUBITEM = 1;

        private int viewType;
        private int groupItemIndex = 0;
        private int subItemIndex = -1;

        private ItemStatus(){
        }
        private int getViewType(){
            return viewType;
        }
        private void setViewType(int viewType){
            this.viewType = viewType;
        }
        private int getGroupItemIndex(){
            return groupItemIndex;
        }
        private void setGroupItemIndex(int groupItemIndex){
            this.groupItemIndex = groupItemIndex;
        }
        private int getSubItemIndex(){
            return subItemIndex;
        }
        private void setSubItemIndex(int subItemIndex){
            this.subItemIndex = subItemIndex;
        }
    }

/* ****************************************************************************************************************************** */
/* ****************************************************************************************************************************** */
    public DeviceAdapter(List<Device> deviceList){
        groupItemStatus = new ArrayList<>();
        this.mDeviceList = deviceList;
        initGroupItemStatus(groupItemStatus);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view;
        RecyclerView.ViewHolder viewHolder = null;
        if(mContext == null)
            mContext = parent.getContext();
        if(viewType == ItemStatus.VIEW_TYPE_GROUPITEM) {
            view = LayoutInflater.from(mContext).inflate(R.layout.device_title_item, parent,false);
            viewHolder = new GroupItemViewHolder(view);
        }
        else if(viewType == ItemStatus.VIEW_TYPE_SUBITEM) {
            view = LayoutInflater.from(mContext).inflate(R.layout.device_content_item, parent, false);
            viewHolder = new SubItemViewHolder(view);
        }
         return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position){
        final ItemStatus itemStatus = getItemStatusByPosition(position);
        final Device device = mDeviceList.get(itemStatus.getGroupItemIndex());
        /* ******************************************************************************** */
        if(itemStatus.getViewType() == ItemStatus.VIEW_TYPE_GROUPITEM){
            final GroupItemViewHolder groupItemViewHolder = (GroupItemViewHolder) viewHolder;
            /* setter */
            groupItemViewHolder.deviceName.setText(device.getGroupItem().getName());
            Glide.with(mContext)
                    .load(device.getGroupItem().getImageId())
                    .into(groupItemViewHolder.deviceImage);
            String status = device.getGroupItem().getStatus();
            groupItemViewHolder.deviceStatus.setText(status);

            /* OnClickListener */
            groupItemViewHolder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int groupItemIndex = itemStatus.getGroupItemIndex();
                    if(!groupItemStatus.get(groupItemIndex)){
                        groupItemStatus.set(groupItemIndex, true);
                        notifyItemRangeInserted(groupItemViewHolder.getAdapterPosition() + 1, device.getSubItems().size());
                    }else {
                        groupItemStatus.set(groupItemIndex, false);
                        notifyItemRangeRemoved(groupItemViewHolder.getAdapterPosition() + 1, device.getSubItems().size());
                    }
                }
            });
            /*OnLongClickListener*/
            groupItemViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = groupItemViewHolder.getAdapterPosition();
                    showBottomDialog(mContext, position, mDeviceList, DeviceAdapter.this);
                    return true;
                }
            });
        }  /* ******************************************************************************** */
        else if(itemStatus.getViewType() == ItemStatus.VIEW_TYPE_SUBITEM){
            final SubItemViewHolder subItemViewHolder = (SubItemViewHolder) viewHolder;
            final DeviceContent deviceContent = (DeviceContent)device.getSubItems().get(itemStatus.getSubItemIndex());
            if(deviceContent.getType().equals("text")) {
                // if it's text type
                subItemViewHolder.deviceContentTextName.setText(deviceContent.getName());
                subItemViewHolder.deviceContentText.setText(deviceContent.getValue());
                subItemViewHolder.deviceContentTextLayout.setVisibility(View.VISIBLE);
                subItemViewHolder.deviceContentImageLayout.setVisibility(View.GONE);
                subItemViewHolder.deviceContentSwitchLayout.setVisibility(View.GONE);

            }else if(deviceContent.getType().equals("image")) {
                subItemViewHolder.deviceContentImageName.setText(deviceContent.getName());
                Glide.with(mContext)
                        .load(deviceContent.getValue())
                        .into(subItemViewHolder.deviceContentImage);
                subItemViewHolder.deviceContentTextLayout.setVisibility(View.GONE);
                subItemViewHolder.deviceContentImageLayout.setVisibility(View.VISIBLE);
                subItemViewHolder.deviceContentSwitchLayout.setVisibility(View.GONE);

            }else if(deviceContent.getType().equals("switch")) {
                subItemViewHolder.deviceContentSwitchName.setText(deviceContent.getName());
                subItemViewHolder.deviceContentSwitch.setChecked(stringToBoolean(deviceContent.getValue()));
                subItemViewHolder.deviceContentTextLayout.setVisibility(View.GONE);
                subItemViewHolder.deviceContentImageLayout.setVisibility(View.GONE);
                subItemViewHolder.deviceContentSwitchLayout.setVisibility(View.VISIBLE);
                subItemViewHolder.deviceContentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                    String deviceName = device.getGroupItem().getName();
                    String deviceContentName = deviceContent.getName();
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                        if(device.getGroupItem().getStatus().equals("offline")) {
                            subItemViewHolder.deviceContentSwitch.setChecked(!isChecked);
                            ToastShowInBottom("Device offline.");
                        }
                        else { //online
                            boolean TrueOrFalse;
                            if (isChecked) {
                                TrueOrFalse = setValueToDeviceContent(deviceName, deviceContentName, "true");
                                if (TrueOrFalse) {
                                    deviceContent.setValue("true");
                                    //mDeviceList.get(position).getSubItems();
                                    ToastShowInBottom(deviceContentName + " turn on.");
                                }
                            } else {
                                TrueOrFalse = setValueToDeviceContent(deviceName, deviceContentName, "false");
                                if (TrueOrFalse) {
                                    deviceContent.setValue("false");
                                    ToastShowInBottom(deviceContentName + " turn off.");
                                }
                            }
                        }
                    }
                });

            }
        }
    }

    private boolean stringToBoolean(String string){
        if(string.equals("true"))
            return true;
        else
            return false;
    }

    @Override
    public int getItemViewType(int position){
        return getItemStatusByPosition(position).getViewType();
    }

    @Override
    public int getItemCount(){
        int itemCount = 0;
        if(mDeviceList.size() == 0){
            return 0;
        }
        for(int i = 0; i < mDeviceList.size(); i++){
            if(groupItemStatus.size() <= i)
                groupItemStatus.add(false);
            if (groupItemStatus.get(i)) {
                itemCount += mDeviceList.get(i).getSubItems().size(); //Add its subitem.size()
            }
            itemCount++;    // Add itself anytime
        }
        return itemCount;
    }

    private void initGroupItemStatus(List groupItemStatus){
        for (int i = 0; i < mDeviceList.size(); i++) {
            groupItemStatus.add(false);
        }
    }

    private ItemStatus getItemStatusByPosition(int position){
        ItemStatus itemStatus = new ItemStatus();

        int i = 0;
        int count = 0;

        for(i = 0; i < groupItemStatus.size(); i++){
            if(count == position){
                itemStatus.setViewType(ItemStatus.VIEW_TYPE_GROUPITEM);
                itemStatus.setGroupItemIndex(i);
                break;
            }else if(count > position){
                itemStatus.setViewType(ItemStatus.VIEW_TYPE_SUBITEM);
                itemStatus.setGroupItemIndex(i - 1);
                itemStatus.setSubItemIndex(position - ( count - mDeviceList.get(i - 1).getSubItems().size()));
                break;
            }
            count++;
            if(groupItemStatus.get(i)){
                count += mDeviceList.get(i).getSubItems().size();
            }
        }

        if(i >= groupItemStatus.size()){
            itemStatus.setGroupItemIndex(i - 1);
            itemStatus.setViewType(ItemStatus.VIEW_TYPE_SUBITEM);
            itemStatus.setSubItemIndex((position - (count - mDeviceList.get(i - 1).getSubItems().size())));
        }
        return itemStatus;
    }

}

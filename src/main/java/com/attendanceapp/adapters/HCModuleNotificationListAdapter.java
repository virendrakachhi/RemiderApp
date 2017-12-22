package com.attendanceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.attendanceapp.R;
import com.attendanceapp.models.HCModuleMessage;

import java.util.ArrayList;
import java.util.List;

public class HCModuleNotificationListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<HCModuleMessage> messagesList = new ArrayList<>();

    public HCModuleNotificationListAdapter(Context context, List<HCModuleMessage> hcModuleMessagesList) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.messagesList = hcModuleMessagesList;
    }

    @Override
    public int getCount() {
        return messagesList.size();
    }

    @Override
    public HCModuleMessage getItem(int position) {
        return messagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        final TextView  othersTimeTextView,meTimeTextView, othersMessageTextView,meMessageTextView,
        othersNameTextView, meNameTextView;
        LinearLayout meChatBox,otherChatBox;


        ViewHolder(View view) {
          //  othersDateTextView = (TextView) view.findViewById(R.id.other);
            othersTimeTextView = (TextView) view.findViewById(R.id.othersTime);
            othersMessageTextView = (TextView) view.findViewById(R.id.othersMessage);
            othersNameTextView = (TextView) view.findViewById(R.id.othersName);

          //  meDateTextView = (TextView) view.findViewById(R.id.dateTextView);
            meTimeTextView = (TextView) view.findViewById(R.id.meTime);
            meMessageTextView = (TextView) view.findViewById(R.id.meMessage);
            meNameTextView = (TextView) view.findViewById(R.id.meName);
            meChatBox = (LinearLayout)view.findViewById(R.id.meChatBox);
            otherChatBox = (LinearLayout)view.findViewById(R.id.othersChatBox);
            meChatBox.setVisibility(LinearLayout.GONE);
            otherChatBox.setVisibility(LinearLayout.INVISIBLE);

        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        bind(getItem(position), view, position);
        return view;
    }


    private void bind(HCModuleMessage messages, View view, int position) {
        ViewHolder holder = (ViewHolder) view.getTag();

//        HCModuleMessage obj = messagesList.get(position);
        if(messages.senderName.matches("Me"))
        {
            holder.otherChatBox.setVisibility(LinearLayout.GONE);
            holder.meChatBox.setVisibility(LinearLayout.VISIBLE);
            holder.meTimeTextView.setText(messages.messageTime);
            holder.meMessageTextView.setText(messages.message);
            holder.meNameTextView.setText(messages.senderName);
            holder.othersMessageTextView.setVisibility(TextView.INVISIBLE);
            holder.othersNameTextView.setVisibility(TextView.INVISIBLE);
            holder.othersTimeTextView.setVisibility(TextView.INVISIBLE);
}
        else {
            holder.otherChatBox.setVisibility(LinearLayout.VISIBLE);
            holder.meChatBox.setVisibility(LinearLayout.GONE);
            holder.othersTimeTextView.setText(messages.messageTime);
            holder.othersMessageTextView.setText(messages.message);
            holder.othersNameTextView.setText(messages.senderName);
            holder.meMessageTextView.setVisibility(TextView.INVISIBLE);
            holder.meNameTextView.setVisibility(TextView.INVISIBLE);
            holder.meTimeTextView.setVisibility(TextView.INVISIBLE);

        }

    }


    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_hc_module_notification, null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }
}

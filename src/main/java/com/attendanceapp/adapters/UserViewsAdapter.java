package com.attendanceapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.attendanceapp.R;
import com.attendanceapp.models.User;
import com.attendanceapp.models.UserRole;

public class UserViewsAdapter extends BaseAdapter {

    private Activity activity;
    private User user;
    private LayoutInflater inflater;
    private ListView userViewsListView;

    public UserViewsAdapter(Activity activity, User user) {
        this.activity = activity;
        this.user = user;
        inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        userViewsListView = (ListView) this.activity.findViewById(R.id.userViewsListView);
        userViewsListView.setAdapter(this);
        userViewsListView.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    public int getCount() {
        return user.getUserRoles().size();
    }

    @Override
    public UserRole getItem(int position) {
        return user.getUserRoles().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        bind(getItem(position), view, position);
        return view;
    }


    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_user_views, null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }

    private void bind(UserRole userRole, View view, int position) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.currentViewType.setText(userRole.toString());
        holder.currentViewType.setTag(holder);
    }

    public class ViewHolder {
        final TextView currentViewType;

        ViewHolder(View view) {
            currentViewType = (TextView) view.findViewById(R.id.currentViewType);
        }
    }

}

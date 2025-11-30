package com.example.lotterypatentpending.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingListState;

import java.util.ArrayList;

/**
 * Adapter for viewing users in a waiting list
 */
public class WaitingListAdapter extends ArrayAdapter<Pair<User, WaitingListState>> {
    private int selectedPos = -1;

    /**
     * Creates a WaitingListAdapter
     * @param context The current context
     * @param list List of users
     */
    public WaitingListAdapter(@NonNull Context context, @NonNull ArrayList<Pair<User, WaitingListState>> list){
        super(context, 0, list);
    }

    /**
     * getView override
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return the view for the waiting list
     */
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_waiting_list, parent, false);
        }

        Pair<User, WaitingListState> user_state = getItem(position);
        TextView userName = convertView.findViewById(R.id.userName);
        TextView waitingState = convertView.findViewById(R.id.waitingState);

        if (user_state != null) {
            User user = user_state.first;
            WaitingListState state = user_state.second;
            if (user != null) {
                userName.setText(user.getName());
            }
            if (state != null) {
                waitingState.setText(state.name());
            }
        }

        //highlights selected row
        convertView.setActivated(position == selectedPos);

        return convertView;
    }

    /**
     * Gets the item the user clicked on
     * @param position index of the item the user clicked on
     */
    public void setSelectedPosition(int position) {
        this.selectedPos = position;
        notifyDataSetChanged();
    }

}

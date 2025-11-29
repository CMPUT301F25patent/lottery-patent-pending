package com.example.lotterypatentpending.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingList;
import com.example.lotterypatentpending.models.WaitingListState;

import java.util.ArrayList;
import java.util.List;
/**
 * Adapter for displaying entries from a waiting list, where each item is a
 * {@code Pair<User, WaitingListState>}. Used to populate the list shown to
 * organizers when reviewing entrants.
 *
 * <p>Each row shows:</p>
 * <ul>
 *   <li>User name</li>
 *   <li>The user's current waiting-list state</li>
 * </ul>
 */
public class WaitingListAdapter extends ArrayAdapter<Pair<User, WaitingListState>> {
    private int selectedPos = -1;
    /**
     * Creates an adapter backed by a list of (User, WaitingListState) pairs.
     *
     * @param context the host Activity or Fragment context
     * @param list    the waiting-list data to display
     */
    public WaitingListAdapter(@NonNull Context context, @NonNull ArrayList<Pair<User, WaitingListState>> list){
        super(context, 0, list);
    }
    /**
     * Returns the view for a given row.
     *
     * <p>Inflates {@code item_waiting_list} if needed and binds the user's
     * name and current waiting-list state.</p>
     *
     * @param position    row index
     * @param convertView recycled view if available
     * @param parent      the ListView or RecyclerView container
     * @return the populated row view
     */
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_waiting_list, parent, false);
        }

        Pair<User, WaitingListState> user_state = getItem(position);
        TextView userName = convertView.findViewById(R.id.userName);
        TextView waitingState = convertView.findViewById(R.id.waitingState);

        if(user_state != null){
            User user = user_state.first;
            WaitingListState state = user_state.second;
            userName.setText(user.getName());
            waitingState.setText(state.name());
        }

        return convertView;
    }
    /**
     * Marks one row as "selected" for UI interactions.
     * This adapter does not style the row by itself; consumers should use
     * this value to drive highlighting logic.
     *
     * @param position the row index to select
     */
    public void setSelectedPosition(int position) {
        this.selectedPos = position;
        notifyDataSetChanged();
    }

}

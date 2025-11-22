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

public class WaitingListAdapter extends ArrayAdapter<Pair<User, WaitingListState>> {

    public WaitingListAdapter(@NonNull Context context, @NonNull ArrayList<Pair<User, WaitingListState>> list){
        super(context, 0, list);
    }

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

}

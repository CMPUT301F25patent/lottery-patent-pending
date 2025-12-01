package com.example.lotterypatentpending.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.util.Pair;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingListState;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 36)
public class WaitingListAdapterTest {
    private Context context;
    private WaitingListAdapter adapter;
    private ArrayList<Pair<User, WaitingListState>> data;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();

        // Prepare sample data
        data = new ArrayList<>();
        data.add(new Pair<>(new User("1", "TestUser1", "test1@example.com", "123", false), WaitingListState.SELECTED));
        data.add(new Pair<>(new User("2", "TestUser2", "test2@example.com", "123", false), WaitingListState.ACCEPTED));

        adapter = new WaitingListAdapter(context, data);
    }

    @Test
    public void testGetCount() {
        assertEquals(2, adapter.getCount());
    }

    @Test
    public void testGetItem() {
        Pair<User, WaitingListState> item = adapter.getItem(0);
        assertEquals("TestUser1", item.first.getName());
        assertEquals(WaitingListState.SELECTED, item.second);
    }

    @Test
    public void testSetSelectedPosition() {
        adapter.setSelectedPosition(1);
        assertEquals(1, adapter.getSelectedPosition());
    }

    @Test
    public void testGetItem_OutOfBounds_ThrowsException() {
        try {
            adapter.getItem(5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testGetView_PopulatesText() {
        LinearLayout parent = new LinearLayout(context);

        // Inflate the first item view
        View view = adapter.getView(0, null, parent);
        assertNotNull(view);

        // Find TextViews by ID
        TextView userNameView = view.findViewById(R.id.userName);
        TextView waitingStateView = view.findViewById(R.id.waitingState);

        // Assert text values
        assertEquals("TestUser1", userNameView.getText().toString());
        assertEquals("SELECTED", waitingStateView.getText().toString());
    }

}

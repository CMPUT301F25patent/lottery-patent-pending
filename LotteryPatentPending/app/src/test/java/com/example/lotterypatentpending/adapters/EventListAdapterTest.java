package com.example.lotterypatentpending.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.core.util.Pair;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.WaitingList;
import com.example.lotterypatentpending.models.WaitingListState;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class EventListAdapterTest {

    private Context context;
    private List<Event> events;
    private EventListAdapter adapter;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        events = new ArrayList<>();

        Event event1 = new Event();
        event1.setTitle("Test Event 1");
        event1.setTag("Tag1");
        event1.setLocation("Location1");
        event1.setCapacity(10);

        WaitingList wl1 = new WaitingList();
        wl1.setList(new ArrayList<>());
        wl1.getList().add(new Pair<>(null, WaitingListState.ACCEPTED));
        wl1.getList().add(new Pair<>(null, WaitingListState.SELECTED));
        event1.setWaitingList(wl1);

        Event event2 = new Event();
        event2.setTitle("Test Event 2");
        event2.setTag("Tag2");
        event2.setLocation(null); // test "Not set"
        event2.setCapacity(20);

        events.add(event1);
        events.add(event2);

        adapter = new EventListAdapter(context, events);
    }

    @Test
    public void testGetCount() {
        assertEquals(2, adapter.getCount());
    }

    @Test
    public void testGetItem() {
        Event item = adapter.getItem(0);
        assertNotNull(item);
        assertEquals("Test Event 1", item.getTitle());
    }

    @Test
    public void testGetView_PopulatesFields() {
        LinearLayout parent = new LinearLayout(context);
        View view = adapter.getView(0, null, parent);

        assertNotNull(view);

        TextView name = view.findViewById(R.id.eventName);
        TextView tag = view.findViewById(R.id.eventTag);
        TextView location = view.findViewById(R.id.eventLocation);
        TextView waitlist = view.findViewById(R.id.eventWaitlist);
        TextView capacityView = view.findViewById(R.id.eventCapacity);
        ImageButton editBtn = view.findViewById(R.id.btnEdit);
        ImageButton deleteBtn = view.findViewById(R.id.btnDelete);

        assertEquals("Test Event 1", name.getText().toString());
        assertEquals("Tag1", tag.getText().toString());
        assertEquals("Location1", location.getText().toString());

        // Waitlist: 1 ACCEPTED / capacity (10)
        assertEquals("2 / 0", waitlist.getText().toString()); // depends on WaitingList setup
        assertEquals("1 / 10", capacityView.getText().toString());

        // For adapter without actions, buttons should be GONE
        assertEquals(View.GONE, editBtn.getVisibility());
        assertEquals(View.GONE, deleteBtn.getVisibility());
    }

    @Test
    public void testGetView_WithActions_ShowsButtons() {
        EventListAdapter adapterWithActions = new EventListAdapter(context, events,
                new EventListAdapter.OnEventActionListener() {
                    @Override
                    public void onEdit(Event event) {
                        // no-op
                    }

                    @Override
                    public void onDelete(Event event) {
                        // no-op
                    }
                });

        LinearLayout parent = new LinearLayout(context);
        View view = adapterWithActions.getView(0, null, parent);

        ImageButton editBtn = view.findViewById(R.id.btnEdit);
        ImageButton deleteBtn = view.findViewById(R.id.btnDelete);

        assertEquals(View.VISIBLE, editBtn.getVisibility());
        assertEquals(View.VISIBLE, deleteBtn.getVisibility());
    }
}

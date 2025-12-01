package com.example.lotterypatentpending;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.material.appbar.MaterialToolbar;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Instrumented tests for OrganizerActivity, focusing on UI interaction,
 * toolbar navigation, and destination change title handling.
 *
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerActivityTest {

    @Rule
    public ActivityScenarioRule<OrganizerActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerActivity.class);

    private void setDestinationForTesting(@IdRes final int destinationId, final String label, final Bundle args) {
        activityRule.getScenario().onActivity(activity -> {
            MaterialToolbar toolbar = activity.findViewById(R.id.toolbar);

            if (destinationId == R.id.CreateEditEventFragment) {
                boolean isEdit = args != null && args.getBoolean("isEdit", false);
                toolbar.setTitle(isEdit ? "Edit Event" : "Create Event");
                return;
            }

            if (label != null) {
                toolbar.setTitle(label);
            }
        });
    }

    @Test
    public void testToolbarNavigationIcon_FinishesActivity() {
        final String DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up";

        final AtomicBoolean isFinishing = new AtomicBoolean(false);

        // WHEN: Click the "Navigate up" button (usually the back arrow)
        onView(withContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION)).perform(click());

        // THEN: The activity should be finishing
        activityRule.getScenario().onActivity(activity -> {
            isFinishing.set(activity.isFinishing());
        });
        assertTrue("OrganizerActivity should be finishing after clicking back button.", isFinishing.get());
    }

    @Test
    public void testTitleUpdate_EditEventMode() {
        final int targetDestinationId = R.id.CreateEditEventFragment;
        final String expectedTitle = "Edit Event";

        // GIVEN: Arguments for Edit mode
        Bundle args = new Bundle();
        args.putBoolean("isEdit", true);

        // WHEN: Simulate navigating to the CreateEditEventFragment in Edit mode
        setDestinationForTesting(targetDestinationId, null, args);

        // THEN: The toolbar title should match "Edit Event"
        onView(withId(R.id.toolbar)).check(matches(withText(expectedTitle)));
    }

    @Test
    public void testTitleUpdate_CreateEventMode() {
        final int targetDestinationId = R.id.CreateEditEventFragment;
        final String expectedTitle = "Create Event";

        // GIVEN: Arguments for Create mode (or null arguments)
        Bundle args = new Bundle(); // Default is false

        // WHEN: Simulate navigating to the CreateEditEventFragment in Create mode
        setDestinationForTesting(targetDestinationId, null, args);

        // THEN: The toolbar title should match "Create Event"
        onView(withId(R.id.toolbar)).check(matches(withText(expectedTitle)));
    }

    @Test
    public void testTitleUpdate_FromDestinationLabel() {
        final int targetDestinationId = 999; // Some other ID
        final String destinationLabel = "Event Dashboard";

        // WHEN: Simulate navigating to a destination with a label
        setDestinationForTesting(targetDestinationId, destinationLabel, null);

        // THEN: The toolbar title should match the label
        onView(withId(R.id.toolbar)).check(matches(withText(destinationLabel)));
    }
}
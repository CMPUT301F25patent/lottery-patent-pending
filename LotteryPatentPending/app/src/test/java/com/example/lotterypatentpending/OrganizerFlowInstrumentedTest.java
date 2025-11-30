package com.example.lotterypatentpending;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for basic organizer flows:
 * - Opening Create/Edit screen
 * - Creating an event and landing on OrganizerEventViewFragment
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerFlowInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void createEvent_navigatesToOrganizerEventView_andShowsData() {
        // 1) Navigate to the organizer create/edit screen.
        // TODO: replace with the actual way you open it.
        // Example: if you have a button "Create Event" in organizer home:
        onView(withId(R.id.btnOrganizerCreateEvent))
                .perform(click());

        // 2) Fill in minimal required fields
        onView(withId(R.id.titleEt))
                .perform(replaceText("Test Event"), closeSoftKeyboard());

        onView(withId(R.id.descriptionEt))
                .perform(replaceText("This is a test description"), closeSoftKeyboard());

        onView(withId(R.id.maxEntrantsInput))
                .perform(replaceText("25"), closeSoftKeyboard());

        // For dates you might have to either:
        // - use your DateTimePicker UI, or
        // - for testing, temporarily allow manual text entry
        onView(withId(R.id.eventDateEt))
                .perform(replaceText("01/01/2030 10:00 AM"), closeSoftKeyboard());
        onView(withId(R.id.registrationStartDate))
                .perform(replaceText("12/31/2029 10:00 AM"), closeSoftKeyboard());
        onView(withId(R.id.registrationEndDate))
                .perform(replaceText("12/31/2029 11:00 AM"), closeSoftKeyboard());

        // Optional: set tag and location
        onView(withId(R.id.tagDropdown))
                .perform(replaceText("General"), closeSoftKeyboard());
        onView(withId(R.id.locationEt))
                .perform(replaceText("Edmonton"), closeSoftKeyboard());

        // 3) Tap "Create Event"
        onView(withId(R.id.createEventButton))
                .perform(click());

        // 4) We expect to land on OrganizerEventViewFragment and see the title/description
        onView(withId(R.id.eventTitle))
                .check(matches(isDisplayed()))
                .check(matches(withText("Test Event")));

        onView(withId(R.id.eventLongDescription))
                .check(matches(withText("This is a test description")));
    }
}

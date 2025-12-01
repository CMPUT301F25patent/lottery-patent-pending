package com.example.lotterypatentpending;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.espresso.intent.rule.IntentsRule;

import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for MainActivity, focusing on UI visibility,
 * navigation Intents, and access control based on user status.
 *
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    private User adminUser;
    private User normalUser;
    private UserEventRepository repository;

    @Rule
    public IntentsRule intentsRule = new IntentsRule();

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        adminUser = new User("admin001", "System Admin", "admin@email.com", "N/A", true);
        normalUser = new User("user001", "Regular User", "user@email.com", "111-222-3333", false);

        repository = UserEventRepository.getInstance();
    }

    @After
    public void tearDown() {
        repository.setUser(null);
    }


    @Test
    public void testAttendeeButton_LaunchesAttendeeActivity() {
        // GIVEN: A user (doesn't matter if normal or admin for this navigation)
        repository.setUser(normalUser);

        // WHEN: Clicking the Attendee button
        onView(withId(R.id.main_button_attendee)).perform(click());

        // THEN: AttendeeActivity is launched
        intended(hasComponent(AttendeeActivity.class.getName()));
    }

    @Test
    public void testOrganizerButton_LaunchesOrganizerActivity() {
        // GIVEN: A user
        repository.setUser(normalUser);

        // WHEN: Clicking the Organizer button
        onView(withId(R.id.main_button_organizer)).perform(click());

        // THEN: OrganizerActivity is launched
        intended(hasComponent(OrganizerActivity.class.getName()));
    }

    @Test
    public void testAdminButton_VisibleForAdminUser() {
        // GIVEN: An admin user is set
        repository.setUser(adminUser);

        // The Activity is launched/recreated with the admin user
        activityRule.getScenario().recreate();

        // THEN: The admin button should be visible
        onView(withId(R.id.main_button_admin)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminButton_HiddenForNormalUser() {
        // GIVEN: A normal user is set
        repository.setUser(normalUser);

        // The Activity is launched/recreated with the normal user
        activityRule.getScenario().recreate();

        // THEN: The admin button should be hidden (GONE is tricky to test, but not(isDisplayed()) is a strong indicator)
        onView(withId(R.id.main_button_admin)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testAdminButtonClick_LaunchesAdminActivity_ForAdminUser() {
        // GIVEN: An admin user is set
        repository.setUser(adminUser);

        // The Activity is launched/recreated with the admin user
        activityRule.getScenario().recreate();

        // WHEN: Clicking the Admin button
        onView(withId(R.id.main_button_admin)).perform(click());

        // THEN: AdminActivity is launched
        intended(hasComponent(AdminActivity.class.getName()));
    }

    @Test
    public void testOnProfileSaved_HidesOverlayAndShowsMainLayout() {
        // GIVEN: MainActivity is showing the registration overlay (e.g., it started as a new user)
        activityRule.getScenario().onActivity(activity -> {
            activity.findViewById(R.id.main_layout).setVisibility(View.GONE);

            repository.setUser(normalUser);

            activity.onProfileSaved();
        });

        // THEN: The main layout should become visible (it was hidden before onboarding)
        onView(withId(R.id.main_layout)).check(matches(isDisplayed()));

        // AND: The createUserOverlay container should be gone
        onView(withId(R.id.createUserOverlay)).check(matches(not(isDisplayed())));
    }
}
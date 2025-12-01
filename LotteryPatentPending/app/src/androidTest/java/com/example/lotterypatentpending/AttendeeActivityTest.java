package com.example.lotterypatentpending;

import android.Manifest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AttendeeActivityTest {

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            );

    /**
     * Helper to ensure UserEventRepository has a non-null user
     * before the activity is created.
     */
    private void seedDummyUser() {
        UserEventRepository repo = UserEventRepository.getInstance();

        User dummy = new User(
                "testUserId",
                "Test User",
                "test@example.com",
                "123-456-7890",
                false          // isAdmin
        );

        // however your repo normally sets the user — adjust if needed
        repo.setUser(dummy);     // if you don't have setUser(), use repo.getUser().setValue(dummy);
    }

    @Test
    public void attendeeActivity_defaultTabIsEvents() {
        seedDummyUser();
        try (ActivityScenario<AttendeeActivity> scenario =
                     ActivityScenario.launch(AttendeeActivity.class)) {

            // Toolbar shows "Events" on launch
            onView(allOf(
                    withText("Events"),
                    withParent(withId(R.id.toolbar)),
                    isDisplayed()
            )).check(matches(isDisplayed()));

            // Bottom nav is visible
            onView(withId(R.id.bottomNav))
                    .check(matches(isDisplayed()));

            // Events item and container present
            onView(withId(R.id.nav_events)).check(matches(isDisplayed()));
            onView(withId(R.id.attendeeContainer)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void attendeeActivity_bottomNavSwitchesFragments() {
        seedDummyUser();
        try (ActivityScenario<AttendeeActivity> scenario =
                     ActivityScenario.launch(AttendeeActivity.class)) {

            // Start: Events
            onView(allOf(
                    withText("Events"),
                    withParent(withId(R.id.toolbar)),
                    isDisplayed()
            )).check(matches(isDisplayed()));

            // Profile
            onView(allOf(withId(R.id.nav_profile), isDisplayed()))
                    .perform(click());
            onView(allOf(
                    withText("Profile"),
                    withParent(withId(R.id.toolbar)),
                    isDisplayed()
            )).check(matches(isDisplayed()));

            // Scan
            onView(allOf(withId(R.id.nav_scan), isDisplayed()))
                    .perform(click());
            onView(allOf(
                    withText("Scan"),
                    withParent(withId(R.id.toolbar)),
                    isDisplayed()
            )).check(matches(isDisplayed()));

            // Back to Events
            onView(allOf(withId(R.id.nav_events), isDisplayed()))
                    .perform(click());
            onView(allOf(
                    withText("Events"),
                    withParent(withId(R.id.toolbar)),
                    isDisplayed()
            )).check(matches(isDisplayed()));
        }
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup &&
                        parentMatcher.matches(parent) &&
                        view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}

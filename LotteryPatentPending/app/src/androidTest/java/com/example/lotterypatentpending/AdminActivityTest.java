package com.example.lotterypatentpending;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.times;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for the AdminActivity, focusing on Intent (navigation)
 * behavior and access control (admin vs. normal user).
 *
 * NOTE: These tests rely on mocking the UserEventRepository to set the current user.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminActivityTest {
    private User adminUser;
    private User normalUser;
    private UserEventRepository repository;

    // Rule for setting up Activity and allowing Intent mocking
    @Rule
    public IntentsRule intentsRule = new IntentsRule();

    public static Intent getAdminActivityIntent() {
        // Create an intent specifically targeting the AdminActivity
        return new Intent(
                ApplicationProvider.getApplicationContext(),
                AdminActivity.class
        );
    }

    // 2. Use the Intent in the ActivityScenarioRule
    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(getAdminActivityIntent());

    @Before
    public void setUp() {
        // Setup mock users
        adminUser = new User("admin001", "System Admin", "admin@email.com", "N/A", true);
        normalUser = new User("user001", "Regular User", "user@email.com", "111-222-3333", false);

        // Get the singleton repository instance
        repository = UserEventRepository.getInstance();
    }

    @After
    public void tearDown() {
        // Reset the user in the repository after each test if necessary
        // to prevent test leakage, though the setup should handle this.
    }


    @Test
    public void testAdminUser_CanLaunchNotificationAdminActivity() {
        // GIVEN: Admin user is active
        repository.setUser(adminUser);

        // WHEN: Clicking the Log button
        onView(withId(R.id.btnLog)).perform(click());

        // THEN: NotificationAdminActivity is launched
        intended(hasComponent(NotificationAdminActivity.class.getName()));
    }

    @Test
    public void testAdminUser_CanLaunchAdminOrganizersActivity() {
        // GIVEN: Admin user is active
        repository.setUser(adminUser);

        // WHEN: Clicking the Remove Organizers button
        onView(withId(R.id.btnRemoveOrganizers)).perform(click());

        // THEN: AdminOrganizersActivity is launched
        intended(hasComponent(AdminOrganizersActivity.class.getName()));
    }

    // NOTE: Fragment navigation (Browse Users/Events/Images) doesn't use Intents,
    // but we can verify fragment containers become visible.

    @Test
    public void testAdminUser_CanNavigateToAdminUsersFragment() {
        // GIVEN: Admin user is active
        repository.setUser(adminUser);

        // WHEN: Clicking the Browse Users button
        onView(withId(R.id.btnBrowseUsers)).perform(click());

        // THEN: Admin content layout is hidden and fragment container is visible
        onView(withId(R.id.adminContent)).check(matches(not(isDisplayed())));
        onView(withId(R.id.adminFragmentContainer)).check(matches(isDisplayed()));
        // Note: A more robust test would check if AdminUsersFragment is in the container.
    }

    @Test
    public void testAdminUser_CanNavigateToAdminImagesFragment() {
        // GIVEN: Admin user is active
        repository.setUser(adminUser);

        // WHEN: Clicking the Images button
        onView(withId(R.id.btnImages)).perform(click());

        // THEN: Admin content layout is hidden and fragment container is visible
        onView(withId(R.id.adminContent)).check(matches(not(isDisplayed())));
        onView(withId(R.id.adminFragmentContainer)).check(matches(isDisplayed()));
        // Note: A more robust test would check if AdminImagesFragment is in the container.
    }

    // --- Normal User Tests (Access Denied) ---

    @Test
    public void testNormalUser_CannotLaunchNotificationAdminActivity() {
        // GIVEN: Normal user is active
        repository.setUser(normalUser);

        // WHEN: Clicking the Log button
        onView(withId(R.id.btnLog)).perform(click());

        // THEN: NotificationAdminActivity is NOT launched, and a Toast is shown
        intended(hasComponent(NotificationAdminActivity.class.getName()), times(0));
        // Note: Espresso doesn't directly assert Toast messages, but we rely on the
        // side effect of the Intent not being launched and the code path executing showAdminDeniedToast().
    }

    @Test
    public void testNormalUser_CannotLaunchAdminOrganizersActivity() {
        // GIVEN: Normal user is active
        repository.setUser(normalUser);

        // WHEN: Clicking the Remove Organizers button
        onView(withId(R.id.btnRemoveOrganizers)).perform(click());

        // THEN: AdminOrganizersActivity is NOT launched
        intended(hasComponent(AdminOrganizersActivity.class.getName()), times(0));
    }

    @Test
    public void testNormalUser_CannotNavigateToAdminUsersFragment() {
        // GIVEN: Normal user is active
        repository.setUser(normalUser);

        // WHEN: Clicking the Browse Users button
        onView(withId(R.id.btnBrowseUsers)).perform(click());

        // THEN: Admin content layout remains visible, fragment container remains hidden
        onView(withId(R.id.adminContent)).check(matches(isDisplayed()));
        onView(withId(R.id.adminFragmentContainer)).check(matches(not(isDisplayed())));
    }
}
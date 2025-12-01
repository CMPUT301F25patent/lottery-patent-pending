package com.example.lotterypatentpending;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Instrumented test for AdminOrganizersActivity using Mockito and Espresso.
 */
@RunWith(AndroidJUnit4.class)
public class AdminOrganizersActivityTest {
    @Mock
    private FirebaseManager mockFirebaseManager;

    private User organizer1;
    private User organizer2;
    private Event eventA;
    private Event eventB;
    private List<User> initialUserList;
    private List<Event> initialEventList;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        FirebaseManager.setInstance(mockFirebaseManager);

        organizer1 = new User("org1_id", "Alice Organizer", "alice@test.com", "123", false);
        organizer2 = new User("org2_id", "Bob Organizer", "bob@test.com", "456", true);

        User nonOrganizer = new User("user_id", "Charlie User", "charlie@test.com", "789", false);

        eventA = new Event("eventA", "descrA", 20, organizer1);
        eventB = new Event("eventB", "descrB", 30, organizer2);

        initialUserList = Arrays.asList(organizer1, organizer2, nonOrganizer);

        initialEventList = Arrays.asList(eventA, eventB);
    }

    /**
     * Helper to create a mocked QuerySnapshot from a list of Users for the getAllUsers call.
     */
    private QuerySnapshot createMockQuerySnapshot(List<User> users) {
        QuerySnapshot mockSnapshot = mock(QuerySnapshot.class);
        List<DocumentSnapshot> mockDocs = new ArrayList<>();

        for (User user : users) {
            DocumentSnapshot doc = mock(DocumentSnapshot.class);

            doAnswer(invocation -> user.getUserId()).when(doc).getId();

            doAnswer(invocation -> user).when(doc).toObject(User.class);

            mockDocs.add(doc);
        }

        doAnswer(invocation -> mockDocs).when(mockSnapshot).iterator();
        doAnswer(invocation -> mockDocs).when(mockSnapshot).getDocuments();

        return mockSnapshot;
    }

    // --- Mocking Setup for Initial Load ---

    private void mockInitialLoad() {
        doAnswer((Answer<Void>) invocation -> {
            FirebaseManager.FirebaseCallback<ArrayList<Event>> callback = invocation.getArgument(0);
            callback.onSuccess(new ArrayList<>(initialEventList));
            return null;
        }).when(mockFirebaseManager).getAllEvents(any());

        QuerySnapshot initialSnapshot = createMockQuerySnapshot(initialUserList);
        doAnswer((Answer<Void>) invocation -> {
            FirebaseManager.FirebaseCallback<QuerySnapshot> callback = invocation.getArgument(0);
            callback.onSuccess(initialSnapshot);
            return null;
        }).when(mockFirebaseManager).getAllUsers(any());
    }

    @Test
    public void testOrganizersAreLoadedCorrectly() {
        mockInitialLoad();

        ActivityScenario.launch(AdminOrganizersActivity.class);

        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())));

        onData(anything())
                .inAdapterView(withId(R.id.userListView))
                .atPosition(0)
                .check(matches(withText(organizer1.getName() + " (" + organizer1.getEmail() + ")")));

        onData(anything())
                .inAdapterView(withId(R.id.userListView))
                .atPosition(1)
                .check(matches(withText(organizer2.getName() + " (" + organizer2.getEmail() + ") [ADMIN]")));

        onView(withText("Charlie User (charlie@test.com)")).check(doesNotExist());
    }

    @Test
    public void testDeleteOrganizer_ConfirmsAndDeleteAndReloads() {
        mockInitialLoad();

        QuerySnapshot initialSnapshot = createMockQuerySnapshot(initialUserList);
        List<User> remainingUserList = Arrays.asList(organizer2, new User("user_id", "Charlie User", "charlie@test.com", "789", false));
        QuerySnapshot remainingSnapshot = createMockQuerySnapshot(remainingUserList);

        doAnswer((Answer<Void>) invocation -> {
            FirebaseManager.FirebaseCallback<ArrayList<Event>> callback = invocation.getArgument(0);
            callback.onSuccess(new ArrayList<>(initialEventList));
            return null;
        }).when(mockFirebaseManager).getAllEvents(any());

        doAnswer((Answer<Void>) invocation -> {
            FirebaseManager.FirebaseCallback<QuerySnapshot> callback = invocation.getArgument(0);
            callback.onSuccess(initialSnapshot);
            return null;
        })
                .doAnswer((Answer<Void>) invocation -> {
                    FirebaseManager.FirebaseCallback<QuerySnapshot> callback = invocation.getArgument(0);
                    callback.onSuccess(remainingSnapshot);
                    return null;
                }).when(mockFirebaseManager).getAllUsers(any());

        ActivityScenario.launch(AdminOrganizersActivity.class);

        onData(anything())
                .inAdapterView(withId(R.id.userListView))
                .atPosition(0)
                .perform(longClick());

        onView(withText("Delete")).perform(click());

        verify(mockFirebaseManager).deleteUser(organizer1.getUserId());

        onData(anything())
                .inAdapterView(withId(R.id.userListView))
                .atPosition(0)
                .check(matches(withText(organizer2.getName() + " (" + organizer2.getEmail() + ") [ADMIN]")));

        onView(withText(organizer1.getName() + " (" + organizer1.getEmail() + ")")).check(doesNotExist());

        onData(anything())
                .inAdapterView(withId(R.id.userListView))
                .atPosition(1)
                .check(doesNotExist());
    }
}
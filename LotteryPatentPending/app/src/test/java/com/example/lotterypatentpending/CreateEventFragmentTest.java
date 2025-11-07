//package com.example.lotterypatentpending;
//
//import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;
//
//import android.widget.EditText;
//
//import androidx.fragment.app.testing.FragmentScenario;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.example.lotterypatentpending.models.Event;
//import com.example.lotterypatentpending.models.FirebaseManager;
//import com.example.lotterypatentpending.models.User;
//import com.example.lotterypatentpending.viewModels.EventViewModel;
//import com.example.lotterypatentpending.viewModels.UserEventRepository;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.lang.reflect.Field;
//import java.time.LocalDateTime;
//
//public class CreateEventFragmentTest {
//
//    @Mock
//    private FirebaseManager mockFirebaseManager;
//
//    @Mock
//    private User mockUser;
//
//    @Mock
//    private UserEventRepository mockUserRepo;
//
//    @Before
//    public void setup() throws Exception {
//        MockitoAnnotations.openMocks(this);
//
//        // Mock UserEventRepository to return our mock user
//        MutableLiveData<User> userLiveData = new MutableLiveData<>();
//        userLiveData.setValue(mockUser);
//        when(mockUserRepo.getUser()).thenReturn(userLiveData);
//
//        // Mock FirebaseManager singleton
//        Field instanceField = FirebaseManager.class.getDeclaredField("instance");
//        instanceField.setAccessible(true);
//        instanceField.set(null, mockFirebaseManager);
//    }
//
//    @Test
//    public void testCreateEventWithReflection() throws Exception {
//        FragmentScenario<CreateEventFragment> scenario =
//                FragmentScenario.launchInContainer(CreateEventFragment.class);
//
//        scenario.onFragment(fragment -> {
//            // Create real EditText values
//            EditText titleEt = new EditText(fragment.getContext());
//            titleEt.setText("Reflection Event");
//
//            EditText descEt = new EditText(fragment.getContext());
//            descEt.setText("Reflection Description");
//
//            EditText locationEt = new EditText(fragment.getContext());
//            locationEt.setText("Reflection Location");
//
//            EditText capacityEt = new EditText(fragment.getContext());
//            capacityEt.setText("20");
//
//            EditText waitingListEt = new EditText(fragment.getContext());
//            waitingListEt.setText("5");
//
//            EditText eventDateEt = new EditText(fragment.getContext());
//            eventDateEt.setText("07/11/2025 10:00 AM");
//
//            EditText regStartEt = new EditText(fragment.getContext());
//            regStartEt.setText("06/11/2025 09:00 AM");
//
//            EditText regEndEt = new EditText(fragment.getContext());
//            regEndEt.setText("07/11/2025 09:00 AM");
//
//            // Use reflection to inject private fields
//            try {
//                setPrivateField(fragment, "titleEt", titleEt);
//                setPrivateField(fragment, "descriptionEt", descEt);
//                setPrivateField(fragment, "locationEt", locationEt);
//                setPrivateField(fragment, "capacityEt", capacityEt);
//                setPrivateField(fragment, "waitingListCapEt", waitingListEt);
//                setPrivateField(fragment, "eventDateEt", eventDateEt);
//                setPrivateField(fragment, "regStartDateEt", regStartEt);
//                setPrivateField(fragment, "regEndDateEt", regEndEt);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            // Call createEvent
//            fragment.createEvent();
//
//            // Verify that FirebaseManager.addEventToDB() was called
//            verify(mockFirebaseManager).addEventToDB(any(Event.class));
//
//            EventViewModel eventviewModel = new ViewModelProvider(fragment.requireActivity())
//                    .get(EventViewModel.class);
//
//            Event createdEvent = eventviewModel.getEvent().getValue();
//            assertEquals("Reflection Event", createdEvent.getTitle());
//            assertEquals("Reflection Description", createdEvent.getDescription());
//            assertEquals("Reflection Location", createdEvent.getLocation());
//            assertEquals(20, createdEvent.getCapacity());
//            assertEquals(5, createdEvent.getWaitingListCapacity());
//            assertNotNull(createdEvent.getDate());
//            assertNotNull(createdEvent.getRegStartDate());
//            assertNotNull(createdEvent.getRegEndDate());
//        });
//    }
//
//    // Helper to set private fields via reflection
//    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
//        Field field = target.getClass().getDeclaredField(fieldName);
//        field.setAccessible(true);
//        field.set(target, value);
//    }
//}
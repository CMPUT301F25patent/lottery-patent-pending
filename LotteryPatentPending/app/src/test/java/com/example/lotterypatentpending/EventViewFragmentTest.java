//package com.example.lotterypatentpending;
//
//import static org.mockito.Mockito.*;
//
//import android.view.View;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.fragment.app.testing.FragmentScenario;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import com.example.lotterypatentpending.models.Event;
//import com.example.lotterypatentpending.models.QRGenerator;
//import com.example.lotterypatentpending.viewModels.EventViewModel;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//@RunWith(AndroidJUnit4.class)
//public class EventViewFragmentTest {
//
//    @Mock
//    EventViewModel mockViewModel;
//
//    @Mock
//    QRGenerator mockQRGenerator;
//
//    private MutableLiveData<Event> eventLiveData;
//
//    @Before
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//        eventLiveData = new MutableLiveData<>();
//        when(mockViewModel.getEvent()).thenReturn(eventLiveData);
//    }
//
//    @Test
//    public void testEventObservation_updatesUI() {
//        FragmentScenario<EventViewFragment> scenario =
//                FragmentScenario.launchInContainer(EventViewFragment.class);
//
//        scenario.onFragment(fragment -> {
//            // Inject mock ViewModel
//            fragment.getActivity().runOnUiThread(() -> {
//                ViewModelProvider provider = new ViewModelProvider(fragment.requireActivity()) {
//                    @Override
//                    public <T extends androidx.lifecycle.ViewModel> T get(Class<T> modelClass) {
//                        if (modelClass == EventViewModel.class) {
//                            return (T) mockViewModel;
//                        }
//                        return super.get(modelClass);
//                    }
//                };
//            });
//
//            // Post a test event
//            Event testEvent = new Event("Test Event", "Desc", 100, null);
//            testEvent.setWaitingListCapacity(50);
//            testEvent.setGeolocationRequired(true);
//            testEvent.setId("event123");
//
//            eventLiveData.postValue(testEvent);
//
//            TextView title = fragment.getView().findViewById(R.id.eventTitle);
//            TextView descr = fragment.getView().findViewById(R.id.eventLongDescription);
//            TextView maxEntrants = fragment.getView().findViewById(R.id.maxEntrants);
//            TextView waitList = fragment.getView().findViewById(R.id.waitingListCap);
//            CheckBox geoCheck = fragment.getView().findViewById(R.id.geoCheck);
//
//            assert(title.getText().toString().equals("Test Event"));
//            assert(descr.getText().toString().equals("Desc"));
//            assert(maxEntrants.getText().toString().equals("Event Capacity: 100"));
//            assert(waitList.getText().toString().equals("Waiting List Capacity: 50"));
//            assert(geoCheck.isChecked());
//        });
//    }
//
//    @Test
//    public void testCheckbox_callsViewModelUpdate() {
//        FragmentScenario<EventViewFragment> scenario =
//                FragmentScenario.launchInContainer(EventViewFragment.class);
//
//        scenario.onFragment(fragment -> {
//            // Inject mock ViewModel
//            fragment.getActivity().runOnUiThread(() -> {
//                ViewModelProvider provider = new ViewModelProvider(fragment.requireActivity()) {
//                    @Override
//                    public <T extends androidx.lifecycle.ViewModel> T get(Class<T> modelClass) {
//                        if (modelClass == EventViewModel.class) {
//                            return (T) mockViewModel;
//                        }
//                        return super.get(modelClass);
//                    }
//                };
//            });
//
//            CheckBox geoCheck = fragment.getView().findViewById(R.id.geoCheck);
//            geoCheck.setChecked(true);
//
//            verify(mockViewModel).updateGeoRequired(true);
//        });
//    }
//
//    @Test
//    public void testGenerateQRCode_setsImageVisible() {
//        FragmentScenario<EventViewFragment> scenario =
//                FragmentScenario.launchInContainer(EventViewFragment.class);
//
//        scenario.onFragment(fragment -> {
//            ImageView qrView = fragment.getView().findViewById(R.id.qrImage);
//            fragment.generateEventQRCode("event123", qrView);
//
//            assert(qrView.getVisibility() == View.VISIBLE);
//        });
//    }
//}
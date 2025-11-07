//package com.example.lotterypatentpending;
//
//import static org.mockito.Mockito.verify;
//
//import androidx.fragment.app.testing.FragmentScenario;
//import androidx.navigation.NavController;
//import androidx.navigation.Navigation;
//import androidx.test.core.app.ApplicationProvider;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mockito;
//
//public class OrganizerActivityFragmentTest {
//
//    private NavController mockNavController;
//
//    @Before
//    public void setup() {
//        mockNavController = Mockito.mock(NavController.class);
//    }
//
//    @Test
//    public void testCreateEventButtonNavigates() {
//        FragmentScenario<OrganizerActivityFragment> scenario =
//                FragmentScenario.launchInContainer(OrganizerActivityFragment.class);
//
//        scenario.onFragment(fragment -> {
//            // Attach mock NavController
//            Navigation.setViewNavController(fragment.getView(), mockNavController);
//
//            // Simulate button click
//            fragment.create_event.performClick();
//
//            // Verify navigation was triggered
//            verify(mockNavController).navigate(R.id.action_main_to_createEvent);
//        });
//    }
//
//    @Test
//    public void testViewEventsButtonNavigates() {
//        FragmentScenario<OrganizerActivityFragment> scenario =
//                FragmentScenario.launchInContainer(OrganizerActivityFragment.class);
//
//        scenario.onFragment(fragment -> {
//            Navigation.setViewNavController(fragment.getView(), mockNavController);
//
//            fragment.view_events.performClick();
//
//            verify(mockNavController).navigate(R.id.action_main_to_viewEventsList);
//        });
//    }
//
//    @Test
//    public void testHomeButtonFinishesActivity() {
//        FragmentScenario<OrganizerActivityFragment> scenario =
//                FragmentScenario.launchInContainer(OrganizerActivityFragment.class);
//
//        scenario.onFragment(fragment -> {
//            // Spy on the hosting activity
//            var spyActivity = Mockito.spy(fragment.requireActivity());
//            fragment.getActivity().runOnUiThread(() -> fragment.home_button.performClick());
//
//            // Verify finish() was called
//            verify(spyActivity).finish();
//        });
//    }
//}
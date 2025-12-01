package com.example.lotterypatentpending;


import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import com.example.lotterypatentpending.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class OrganizerActivityCreateTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
"android.permission.POST_NOTIFICATIONS");

    @Test
    public void organizerActivityCreateTest() {
    ViewInteraction appCompatEditText = onView(
allOf(withId(R.id.et_name),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
1),
1),
isDisplayed()));
    appCompatEditText.perform(replaceText("Test"), closeSoftKeyboard());
    
    ViewInteraction appCompatEditText2 = onView(
allOf(withId(R.id.et_email),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
2),
1),
isDisplayed()));
    appCompatEditText2.perform(replaceText("t@gmail.co"), closeSoftKeyboard());
    
    ViewInteraction materialButton = onView(
allOf(withId(R.id.btn_save), withText("Save"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
0),
4),
isDisplayed()));
    materialButton.perform(click());
    
    ViewInteraction appCompatEditText3 = onView(
allOf(withId(R.id.et_email), withText("t@gmail.co"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
2),
1),
isDisplayed()));
    appCompatEditText3.perform(replaceText("t@gmail.com"));
    
    ViewInteraction appCompatEditText4 = onView(
allOf(withId(R.id.et_email), withText("t@gmail.com"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
2),
1),
isDisplayed()));
    appCompatEditText4.perform(closeSoftKeyboard());
    
    ViewInteraction materialButton2 = onView(
allOf(withId(R.id.main_button_organizer), withText("Organize Event"),
childAtPosition(
allOf(withId(R.id.main_layout),
childAtPosition(
withId(R.id.main),
0)),
3),
isDisplayed()));
    materialButton2.perform(click());
    
    ViewInteraction materialButton3 = onView(
allOf(withId(R.id.create_event), withText("Create Event"),
childAtPosition(
childAtPosition(
withId(R.id.organizer_nav_host),
0),
1),
isDisplayed()));
    materialButton3.perform(click());
    
    ViewInteraction appCompatEditText5 = onView(
allOf(withId(R.id.titleEt),
childAtPosition(
childAtPosition(
withClassName(is("androidx.core.widget.NestedScrollView")),
0),
3),
isDisplayed()));
    appCompatEditText5.perform(replaceText("TestEve"), closeSoftKeyboard());
    
    ViewInteraction materialAutoCompleteTextView = onView(
allOf(withId(R.id.tagDropdown),
childAtPosition(
childAtPosition(
withId(R.id.tagLayout),
0),
0),
isDisplayed()));
    materialAutoCompleteTextView.perform(click());
    
    DataInteraction materialTextView = onData(anything())
.inAdapterView(childAtPosition(
withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
0))
.atPosition(3);
    materialTextView.perform(click());
    
    ViewInteraction appCompatEditText6 = onView(
allOf(withId(R.id.descriptionEt),
childAtPosition(
childAtPosition(
withClassName(is("androidx.core.widget.NestedScrollView")),
0),
10),
isDisplayed()));
    appCompatEditText6.perform(replaceText("test"), closeSoftKeyboard());
    
    ViewInteraction appCompatEditText7 = onView(
allOf(withId(R.id.locationEt),
childAtPosition(
childAtPosition(
withClassName(is("androidx.core.widget.NestedScrollView")),
0),
12),
isDisplayed()));
    appCompatEditText7.perform(replaceText("testplace"), closeSoftKeyboard());
    
    ViewInteraction materialTextView2 = onView(
allOf(withId(R.id.eventDateEt),
childAtPosition(
childAtPosition(
withClassName(is("androidx.core.widget.NestedScrollView")),
0),
14),
isDisplayed()));
    materialTextView2.perform(click());
    
    ViewInteraction appCompatImageButton = onView(
allOf(withClassName(is("androidx.appcompat.widget.AppCompatImageButton")), withContentDescription("Next month"),
childAtPosition(
allOf(withClassName(is("android.widget.DayPickerView")),
childAtPosition(
withClassName(is("com.android.internal.widget.DialogViewAnimator")),
0)),
2)));
    appCompatImageButton.perform(scrollTo(), click());
    
    ViewInteraction materialButton4 = onView(
allOf(withId(android.R.id.button1), withText("OK"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.ScrollView")),
0),
3)));
    materialButton4.perform(scrollTo(), click());
    
    ViewInteraction materialButton5 = onView(
allOf(withId(android.R.id.button1), withText("OK"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.ScrollView")),
0),
3)));
    materialButton5.perform(scrollTo(), click());
    
    ViewInteraction materialTextView3 = onView(
allOf(withId(R.id.registrationStartDate),
childAtPosition(
childAtPosition(
withClassName(is("androidx.core.widget.NestedScrollView")),
0),
16),
isDisplayed()));
    materialTextView3.perform(click());
    
    ViewInteraction appCompatImageButton2 = onView(
allOf(withClassName(is("androidx.appcompat.widget.AppCompatImageButton")), withContentDescription("Next month"),
childAtPosition(
allOf(withClassName(is("android.widget.DayPickerView")),
childAtPosition(
withClassName(is("com.android.internal.widget.DialogViewAnimator")),
0)),
2)));
    appCompatImageButton2.perform(scrollTo(), click());
    
    ViewInteraction materialButton6 = onView(
allOf(withId(android.R.id.button1), withText("OK"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.ScrollView")),
0),
3)));
    materialButton6.perform(scrollTo(), click());
    
    ViewInteraction materialButton7 = onView(
allOf(withId(android.R.id.button1), withText("OK"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.ScrollView")),
0),
3)));
    materialButton7.perform(scrollTo(), click());
    
    ViewInteraction materialTextView4 = onView(
allOf(withId(R.id.registrationEndDate),
childAtPosition(
childAtPosition(
withClassName(is("androidx.core.widget.NestedScrollView")),
0),
18),
isDisplayed()));
    materialTextView4.perform(click());
    
    ViewInteraction appCompatImageButton3 = onView(
allOf(withClassName(is("androidx.appcompat.widget.AppCompatImageButton")), withContentDescription("Next month"),
childAtPosition(
allOf(withClassName(is("android.widget.DayPickerView")),
childAtPosition(
withClassName(is("com.android.internal.widget.DialogViewAnimator")),
0)),
2)));
    appCompatImageButton3.perform(scrollTo(), click());
    
    ViewInteraction materialButton8 = onView(
allOf(withId(android.R.id.button1), withText("OK"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.ScrollView")),
0),
3)));
    materialButton8.perform(scrollTo(), click());
    
    ViewInteraction materialButton9 = onView(
allOf(withId(android.R.id.button1), withText("OK"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.ScrollView")),
0),
3)));
    materialButton9.perform(scrollTo(), click());
    
    ViewInteraction appCompatEditText8 = onView(
allOf(withId(R.id.maxEntrantsInput),
childAtPosition(
childAtPosition(
withClassName(is("androidx.core.widget.NestedScrollView")),
0),
20),
isDisplayed()));
    appCompatEditText8.perform(replaceText("10"), closeSoftKeyboard());
    
    ViewInteraction materialButton10 = onView(
allOf(withId(R.id.createEventButton), withText("Create Event"),
childAtPosition(
childAtPosition(
withClassName(is("androidx.core.widget.NestedScrollView")),
0),
24),
isDisplayed()));
    materialButton10.perform(click());
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
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup)parent).getChildAt(position));
            }
        };
    }
}

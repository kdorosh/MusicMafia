package mattnkev.cs.tufts.edu.musicmafia;

import android.content.ComponentName;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.EditText;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import mattnkev.cs.tufts.edu.musicmafia.activities.EventLoginActivity;
import mattnkev.cs.tufts.edu.musicmafia.activities.PlaylistMakingActivity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class ApplicationTest {

    private String mEventName, mPassword, mEventNameInvalid;

    @Rule
    public ActivityTestRule<EventLoginActivity> mActivityRule = new IntentsTestRule<>(
            EventLoginActivity.class);

    @Before
    public void initValidString() {
        mEventName = "event";
        mPassword = "pass";
        mEventNameInvalid = "this has whitespace";
    }

    //@Test
    public void changeActivityTest() {

        // Type text and then press the button.
        onView(withId(R.id.event_name))
                .perform(typeText(mEventName), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText(mPassword), closeSoftKeyboard());
        onView(withId(R.id.validate_credentials_button)).perform(click());

        // Check that the activity was changed
        intended(hasComponent(new ComponentName(getTargetContext(), PlaylistMakingActivity.class)));

    }

    //@Test
    public void invalidEventNameTest() {

        // Type text and then press the button.
        onView(withId(R.id.event_name))
                .perform(typeText(mEventNameInvalid), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText(mPassword), closeSoftKeyboard());
        onView(withId(R.id.validate_credentials_button)).perform(click());

        // Check that the activity was NOT changed by ensuring the proper error was displayed
        onView(withId(R.id.event_name)).check(matches(hasErrorText("This Event ID has whitespace")));

    }


    private static Matcher<? super View> hasErrorText(String expectedError) {
        return new ErrorTextMatcher(expectedError);
    }

    private static class ErrorTextMatcher extends TypeSafeMatcher<View> {
        private final String expectedError;

        private ErrorTextMatcher(String expectedError) {
            this.expectedError = checkNotNull(expectedError);
        }

        @Override
        public boolean matchesSafely(View view) {
            if (!(view instanceof EditText)) {
                return false;
            }
            EditText editText = (EditText) view;
            return expectedError.equals(editText.getError());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("with error: " + expectedError);
        }
    }
}
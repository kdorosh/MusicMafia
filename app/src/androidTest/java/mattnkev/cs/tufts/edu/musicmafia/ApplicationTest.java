package mattnkev.cs.tufts.edu.musicmafia;

import android.content.ComponentName;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import mattnkev.cs.tufts.edu.musicmafia.activities.EventLoginActivity;
import mattnkev.cs.tufts.edu.musicmafia.activities.PlaylistMakingActivity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class ApplicationTest {

    private String mEventName, mPassword;

    @Rule
    public ActivityTestRule<EventLoginActivity> mActivityRule = new IntentsTestRule<>(
            EventLoginActivity.class);

    @Before
    public void initValidString() {
        mEventName = "event";
        mPassword = "pass";
    }

    @Test
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

// TODO: add non ASCII(search song and add first) test

}
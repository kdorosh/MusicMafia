package mattnkev.cs.tufts.edu.musicmafia.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import mattnkev.cs.tufts.edu.musicmafia.R;
import mattnkev.cs.tufts.edu.musicmafia.Utils;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * A login screen that offers login via event name/event password.
 */
public class EventLoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private boolean mIsHost;

    // UI references.
    private EditText mEventView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_event_login);
        // Set up the login form.

        mEventView = (EditText) findViewById(R.id.event_name);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button validateCredentialsButton = (Button) findViewById(R.id.validate_credentials_button);
        if (validateCredentialsButton != null)
            validateCredentialsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid event name, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEventView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String eventName = mEventView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.userType);
        // get selected radio button from radioGroup
        int selectedId = 0;
        if (radioGroup != null)
            selectedId = radioGroup.getCheckedRadioButtonId();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid eventName.
        if (TextUtils.isEmpty(eventName)) {
            mEventView.setError(getString(R.string.error_field_required));
            focusView = mEventView;
            cancel = true;
        } else if (!isEventNameValid(eventName)) {
            mEventView.setError(getString(R.string.error_invalid_eventName));
            focusView = mEventView;
            cancel = true;
        }

        // only check password if Event Name was valid. results in Cleaner UI
        if (!cancel)
        {
            // Check for a valid password
            if (TextUtils.isEmpty(password)) {
                mPasswordView.setError(getString(R.string.error_field_required));
                focusView = mPasswordView;
                cancel = true;
            } else if (!isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            Button radioButton = (RadioButton) findViewById(selectedId);

            if (radioButton != null)
                mIsHost = radioButton.getText().equals("Host");

            // kick off a background task to perform the user login attempt.
            mAuthTask = new UserLoginTask(eventName, password);
            mAuthTask.execute((Void) null);

        }
    }

    private void changeActivity(String user_type, String eventName, String password){
        Intent intent = new Intent(this, PlaylistMakingActivity.class);
        intent.putExtra("USER_TYPE", user_type);
        intent.putExtra("EVENT_NAME", eventName);
        intent.putExtra("PASSWORD", password);
        startActivity(intent);
        finish();
    }

    private boolean isEventNameValid(String eventName) {
        // Regex: String with at least 1 character where all characters are not whitespace
        return eventName.length() > Utils.MIN_EVENT_NAME_LENGTH && eventName.matches("\\S+");
    }

    private boolean isPasswordValid(String password) {
        // Regex: String with at least 1 character where all characters are not whitespace
        return password.length() > Utils.MIN_PASSWORD_LENGTH && password.matches("\\S+");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEventName;
        private final String mPassword;
        private String mStatus;

        UserLoginTask(String eventName, String password) {
            mEventName = eventName;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean successfulLogin = false;
            try {
                if (mIsHost) {
                    String resp = Utils.attemptPOST("create",
                            mEventName, mPassword,
                            new String[] {},
                            new String[] {});
                    mStatus = Utils.parseRespForStatus(resp);
                    if (mStatus.equals("OK")){
                        successfulLogin = true;
                    }
                }
                else {
                    String resp = Utils.attemptGET(Utils.SERVER_URL, "guestLogin",
                            mEventName, mPassword,
                            new String[] {},
                            new String[] {});
                    mStatus = Utils.parseRespForStatus(resp);
                    if (mStatus.equals("OK")){
                        successfulLogin = true;
                    }
                }

            } catch (Exception ex) {
                // error message will be displayed via mStatus when this returns false
                return false;
            }

            return successfulLogin;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                String user_type;
                if (mIsHost) {
                    user_type = "Host";
                } else { user_type = "Guest"; }

                changeActivity(user_type, mEventName, mPassword);
            } else {
                mEventView.setError(mStatus);
                mEventView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}


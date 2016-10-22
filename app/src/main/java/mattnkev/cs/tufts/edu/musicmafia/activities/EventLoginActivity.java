package mattnkev.cs.tufts.edu.musicmafia.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
    private View mProgressView;
    private View mLoginFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

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
        String eventName = mEventView.getText().toString();
        String password = mPasswordView.getText().toString();

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.userType);
        // get selected radio button from radioGroup
        int selectedId = 0;
        if (radioGroup != null)
            selectedId = radioGroup.getCheckedRadioButtonId();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid eventName address.
        if (TextUtils.isEmpty(eventName)) {
            mEventView.setError(getString(R.string.error_field_required));
            focusView = mEventView;
            cancel = true;
        } else if (!isEventNameValid(eventName)) {
            mEventView.setError(getString(R.string.error_invalid_eventName));
            focusView = mEventView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            //TODO: remove showProgress(bool) later if desired
            //showProgress(true);
            mAuthTask = new UserLoginTask(eventName, password);
            mAuthTask.execute((Void) null);

            Button radioButton = (RadioButton) findViewById(selectedId);

            if (radioButton != null)
                mIsHost = radioButton.getText().equals("Host");

        }
    }

    private void changeActivity(String user_type, String eventName, String password){
        Intent intent = new Intent(this, PlaylistMakingActivity.class);
        intent.putExtra("USER_TYPE", user_type);
        intent.putExtra("EVENT_NAME", eventName);
        intent.putExtra("PASSWORD", password);
        startActivity(intent);
    }

    private boolean isEventNameValid(String eventName) {
        return eventName.length() > Utils.MIN_EVENT_NAME_LENGTH;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > Utils.MIN_PASSWORD_LENGTH;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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
                    mStatus = Utils.attemptPOST(Utils.SERVER_URL, "create",
                            new String[]{"EventName", "password"},
                            new String[] {mEventName, mPassword});
                    if (mStatus.equals("OK")){
                        successfulLogin = true;
                    }
                }
                else {
                    mStatus = Utils.attemptGET(Utils.SERVER_URL, "guestLogin",
                            new String[]{"EventName", "password"},
                            new String[] {mEventName, mPassword});
                    if (mStatus.equals("OK")){
                        successfulLogin = true;
                    }
                }

            } catch (Exception ex) {
                return false;
            }

            return successfulLogin;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

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
            showProgress(false);
        }
    }
}

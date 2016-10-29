package mattnkev.cs.tufts.edu.musicmafia.activities;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import mattnkev.cs.tufts.edu.musicmafia.R;
import mattnkev.cs.tufts.edu.musicmafia.Utils;
import mattnkev.cs.tufts.edu.musicmafia.fragments.EventPlaylistFragment;
import mattnkev.cs.tufts.edu.musicmafia.fragments.SearchSongsFragment;

public class PlaylistMakingActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{
    private static final String CLIENT_ID = "fe81735360154ee7921c12078d656a97";
    private static final String REDIRECT_URI = "android-app-login://callback";

    private Player mPlayer;
    private final FragmentManager mFragmentManager = getSupportFragmentManager();
    private final Fragment mEventPlaylistFragment = new EventPlaylistFragment();
    private final Fragment mSearchSongsFragment = new SearchSongsFragment();
    private BottomBar mBottomBar;

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_making);

        mFragmentManager.beginTransaction().add(R.id.listFragment, mEventPlaylistFragment).commit();
        mFragmentManager.beginTransaction().add(R.id.listFragment, mSearchSongsFragment).commit();

        if (Utils.isHost(getIntent())) {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                    AuthenticationResponse.Type.TOKEN,
                    REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "streaming", "playlist-read-private",
                    "playlist-read-collaborative", "playlist-modify-public", "playlist-modify-private"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        }

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.event_playlist) {
                    mFragmentManager.beginTransaction()
                            //.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .hide(mSearchSongsFragment)
                            .show(mEventPlaylistFragment)
                            .commit();
                }
//              else if (tabId == R.id.search_artists) {
//                    //TODO: add search fragment for artists
//                }
                else if (tabId == R.id.search_songs) {
                    mFragmentManager.beginTransaction()
                            //.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .hide(mEventPlaylistFragment)
                            .show(mSearchSongsFragment)
                            .commit();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                final Utils.EventData eventData = new Utils.EventData(this);
                // Provides Spotify credentials to server
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String status = Utils.attemptGET(Utils.SERVER_URL, "createPlaylist",
                        new String[]{"EventName", "password", "AccessToken", "redirect_uri"},
                        new String[]{eventData.getEventName(), eventData.getPassword(), response.getAccessToken(), REDIRECT_URI});

                        if (!status.equals("OK"))
                            Utils.displayMsg(PlaylistMakingActivity.this, status);

                    }
                });
                thread.start();


                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(mattnkev.cs.tufts.edu.musicmafia.activities.PlaylistMakingActivity.this);
                        mPlayer.addNotificationCallback(mattnkev.cs.tufts.edu.musicmafia.activities.PlaylistMakingActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("SpotifyTest", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(int i) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    /**  END OF SPOTIFY SETUP BOILERPLATE  **/


    public void setBottomBarVisibility(boolean isVisible){
        mBottomBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void pauseButton(MenuItem mi) {
        if (mPlayer != null)
            mPlayer.pause(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // only add the pause button if we aren't the host
        if (Utils.isHost(getIntent())) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_playlist_making_activity, menu);
        }
        return true;
    }

}
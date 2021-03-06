package mattnkev.cs.tufts.edu.musicmafia.activities;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.List;

import mattnkev.cs.tufts.edu.musicmafia.R;
import mattnkev.cs.tufts.edu.musicmafia.SongData;
import mattnkev.cs.tufts.edu.musicmafia.Utils;
import mattnkev.cs.tufts.edu.musicmafia.fragments.EventPlaylistFragment;
import mattnkev.cs.tufts.edu.musicmafia.fragments.NowPlayingFragment;
import mattnkev.cs.tufts.edu.musicmafia.fragments.SearchSongsFragment;

public class PlaylistMakingActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{
    private static final String CLIENT_ID = "fe81735360154ee7921c12078d656a97";
    private static final String REDIRECT_URI = "android-app-login://callback";

    private Player mPlayer;
    private final FragmentManager mFragmentManager = getSupportFragmentManager();
    private final EventPlaylistFragment mEventPlaylistFragment = new EventPlaylistFragment();
    private final SearchSongsFragment mSearchSongsFragment = new SearchSongsFragment();
    private final NowPlayingFragment mNowPlayingFragment = new NowPlayingFragment();
    private BottomBar mBottomBar;

    private boolean login_success = false;

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_making);

        mFragmentManager.beginTransaction().add(R.id.listFragment, mEventPlaylistFragment).commit();
        mFragmentManager.beginTransaction().add(R.id.listFragment, mSearchSongsFragment).commit();
        mFragmentManager.beginTransaction().add(R.id.listFragment, mNowPlayingFragment).commit();

        if (Utils.isHost(getIntent())) {
            askForSpotifyCredentials();
        }

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (!login_success){
                    mFragmentManager.beginTransaction()
                            //.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .hide(mSearchSongsFragment)
                            .hide(mNowPlayingFragment)
                            .show(mEventPlaylistFragment)
                            .commit();
                    askForSpotifyCredentials();
                    return;
                }

                if (tabId == R.id.event_playlist) {
                    mFragmentManager.beginTransaction()
                            //.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .hide(mSearchSongsFragment)
                            .hide(mNowPlayingFragment)
                            .show(mEventPlaylistFragment)
                            .commit();
                }
                else if (tabId == R.id.now_playing) {
                    mFragmentManager.beginTransaction()
                            //.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .hide(mEventPlaylistFragment)
                            .hide(mSearchSongsFragment)
                            .show(mNowPlayingFragment)
                            .commit();
                }
                else if (tabId == R.id.search_songs) {
                    mFragmentManager.beginTransaction()
                            //.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .hide(mEventPlaylistFragment)
                            .hide(mNowPlayingFragment)
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
                                eventData.getEventName(), eventData.getPassword(),
                                new String[]{"AccessToken", "redirect_uri"},
                                new String[]{ response.getAccessToken(), REDIRECT_URI});
                        status = Utils.parseRespForStatus(status);
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
                        Utils.displayMsg(PlaylistMakingActivity.this, "could not initialize player: " + throwable.getMessage());
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
        //Utils.displayMsg(PlaylistMakingActivity.this, "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Utils.displayMsg(PlaylistMakingActivity.this, "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        login_success = true;
    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(int i) {
        Utils.displayMsg(PlaylistMakingActivity.this, "Log in failed; do you have premium?");
        login_success = false;
    }

    @Override
    public void onTemporaryError() {
        Utils.displayMsg(PlaylistMakingActivity.this, "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
    }

    /**  END OF SPOTIFY SETUP BOILERPLATE  **/


    public void setBottomBarVisibility(boolean isVisible){
        mBottomBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void pauseSong(){
        if (mPlayer != null)
            mPlayer.pause(null);
    }

    public long getCurrentPosition(){
        return mPlayer != null ? mPlayer.getPlaybackState().positionMs : 0;
    }

    public void seekTo(double startTime){
        SongData data = mEventPlaylistFragment.getSongsData(0);
        if (mPlayer != null && data!= null && data.getURI() != null) {
            mPlayer.playUri(null, data.getURI(), 0, (int) startTime);
        }
    }

    public int getDuration(){
        SongData data = mEventPlaylistFragment.getSongsData(0);
        if (data != null)
            return data.getDuration();
        else
            return 0;
    }

    public SongData getCurrSongData(){
        return mEventPlaylistFragment.getSongsData(0);
    }

    public List<Fragment> getFragments(){
        return mFragmentManager.getFragments();
    }

    private void askForSpotifyCredentials() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "playlist-read-private",
                "playlist-read-collaborative", "playlist-modify-public", "playlist-modify-private"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

}
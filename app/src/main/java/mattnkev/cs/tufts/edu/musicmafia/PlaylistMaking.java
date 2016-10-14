package mattnkev.cs.tufts.edu.musicmafia;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
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
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;

public class PlaylistMaking extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{
    private static final String CLIENT_ID = "fe81735360154ee7921c12078d656a97";
    private static final String REDIRECT_URI = "android-app-login://callback";

    private Player mPlayer;
    private Menu mOptionsMenu;
    private final FragmentManager fm = getSupportFragmentManager();
    private final Fragment eventPlaylistFragment = new EventPlaylistFragment();
    private final Fragment searchSongsFragment = new SearchSongsFragment();
    private BottomBar bottomBar;

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fm.beginTransaction().add(R.id.listFragment, eventPlaylistFragment).commit();
        fm.beginTransaction().add(R.id.listFragment, searchSongsFragment).commit();

        setContentView(R.layout.activity_playlist_making);

        boolean isHost = true;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("USER_TYPE");
            //The key argument here must match that used in the other activity
            if (value.equals("Guest"))
                isHost = false;
        }

        if (isHost) {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                    AuthenticationResponse.Type.TOKEN,
                    REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "streaming", "playlist-read-private",
                    "playlist-read-collaborative", "playlist-modify-public", "playlist-modify-private"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        }

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.event_playlist) {
                    fm.beginTransaction()
                            //.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .hide(searchSongsFragment)
                            .commit();

                    fm.beginTransaction()
                            //.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .show(eventPlaylistFragment)
                            .commit();

                    //if (mOptionsMenu!=null)
                    //    mOptionsMenu.findItem(R.id.search).setVisible(false);

                    //TODO: really really hacky (gets hidden but we re-show)
                    bottomBar.setVisibility(View.VISIBLE);
                }
                if (tabId == R.id.search_artists) {

                }
                if (tabId == R.id.search_songs) {
                    fm.beginTransaction()
                            //.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .hide(eventPlaylistFragment)
                            .commit();

                    fm.beginTransaction()
                            //.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                            .show(searchSongsFragment)
                            .commit();

                    //if (mOptionsMenu!=null)
                    //    mOptionsMenu.findItem(R.id.search).setVisible(true);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    String eventName = extras.getString("EVENT_NAME");
                    String password = extras.getString("PASSWORD");
                    //The key argument here must match that used in the other activity
                    provideSpotifyAccessTokenToServer(eventName, password, response.getAccessToken(), REDIRECT_URI);

                }

                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(mattnkev.cs.tufts.edu.musicmafia.PlaylistMaking.this);
                        mPlayer.addNotificationCallback(mattnkev.cs.tufts.edu.musicmafia.PlaylistMaking.this);
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
    private boolean provideSpotifyAccessTokenToServer(final String eventName, final String password, final String accessToken, final String redirectURI) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    String urlString = "http://52.40.236.184:5000/createPlaylist"
                            +"?EventName="+eventName+"&password="+password
                            +"&AccessToken="+accessToken+"&redirect_uri="+redirectURI;
                    URL url = new URL(urlString);
                    conn = (HttpURLConnection)url.openConnection();
                    InputStream in = conn.getInputStream();
                    StringBuilder sb = new StringBuilder();
                    for (int c; (c = in.read()) >= 0;)
                        sb.append((char)c);
                    String response = sb.toString();
                    JSONObject data = new JSONObject(response);
                    if(data.getString("Status").equals("OK")){
                        //TODO: only proceed on success
                    } else {
                        //TODO: add good failure messages
                    }
                } catch (Exception ex){
                    Log.d("MainActivity", ex.toString());
                } finally {
                    if (conn != null)
                        conn.disconnect();
                }
            }
        });
        thread.start();

        return true;
    }

    public void setBottomBarVisibility(boolean isVisible){
        if (isVisible) {
            bottomBar.setVisibility(View.VISIBLE);
        } else {
            bottomBar.setVisibility(View.GONE);
        }
    }

    public void pauseButton(MenuItem mi) {
        // handle click here
        mPlayer.pause(null);
    }

    public void spotifySearch(final String query){

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    HttpURLConnection urlConnection = null;
                    try {
                        String formattedQuery = query.replace(" ", "%20");
                        URL url = new URL("https://api.spotify.com/v1/search?q="+formattedQuery+"&type=track&market=US");
                        urlConnection = (HttpURLConnection) url.openConnection();
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        String resp = readStream(in);
                        Log.d("MainActivity", resp);

                        JSONObject data = new JSONObject(resp);
                        JSONObject tracks = data.getJSONObject("tracks");
                        JSONArray entries = tracks.getJSONArray("items");

                        String firstURI = "";
                        final String[] searchListViewSongs = new String[20];
                        final String[] searchListViewURIs = new String[20];//new String[entries.length()];
                        //mListViewVals.clear();
                        for (int i = 0; i < 20;i++){//entries.length(); i++) {
                            JSONObject entry = entries.getJSONObject(i);

                            String name = entry.getString("uri");
                            searchListViewURIs[i] = name;
                            if(i==0){firstURI = name; }

                            name = entry.getString("name");
                            //mListViewVals.add(name);
                            searchListViewSongs[i] = name;
                        }
                        final String URI = firstURI;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //mAdapter.updateVals(mListViewVals);
                                //mAdapter.notifyDataSetChanged();
                                //addSong(URI);
                                //searchSongsFragment.updateListView();
                                updateSearchFrag(searchListViewSongs, searchListViewURIs);

                            }
                        });
                        mPlayer.playUri(null, firstURI, 0, 0);
                    }
                    catch (Exception ex) {
                        Log.d("MainActivity", ex.toString());
                    }
                    finally {
                        urlConnection.disconnect();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void updateSearchFrag(String[] songs, String[] URIs){
        ((SearchSongsFragment)this.getSupportFragmentManager().findFragmentById(R.id.listFragment)).updateListView(songs, songs, URIs);
    }

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playlists, menu);

        mOptionsMenu = menu;
        /*mOptionsMenu.findItem(R.id.search).setVisible(false); //first fragment is playlist

        MenuItem searchItem = menu.findItem(R.id.search);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                spotifySearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });*/

        return true;
    }

}
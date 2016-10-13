package mattnkev.cs.tufts.edu.musicmafia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class EventPlaylistFragment extends Fragment implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{
    private static final String CLIENT_ID = "fe81735360154ee7921c12078d656a97";
    private static final String REDIRECT_URI = "android-app-login://callback";

    private Player mPlayer;
    private ListView mListView;
    private ArrayList<String> mListViewVals = new ArrayList<String>();
    private MySimpleArrayAdapter mAdapter;

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity  =  super.getActivity();
        RelativeLayout   rlLayout    = (RelativeLayout)    inflater.inflate(R.layout.event_playlist_layout, container, false);
//
//        boolean isHost = true;
//        Bundle extras = super.getActivity().getIntent().getExtras();
//        if (extras != null) {
//            String value = extras.getString("USER_TYPE");
//            //The key argument here must match that used in the other activity
//            if (value.equals("Guest"))
//                isHost = false;
//        }
//
//        if (isHost) {
//            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
//                    AuthenticationResponse.Type.TOKEN,
//                    REDIRECT_URI);
//            builder.setScopes(new String[]{"user-read-private", "streaming"});
//            AuthenticationRequest request = builder.build();
//
//            AuthenticationClient.openLoginActivity(super.getActivity(), REQUEST_CODE, request);
//        }
//
        mListView = (ListView)rlLayout.findViewById(R.id.main_list_view);

        String[] vals = new String[] { "Android", "iPhoneReallyReallyReallyLongName", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
                "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
                "Android", "iPhone", "WindowsMobile" };
        for(int i=0; i<vals.length;i++)
            mListViewVals.add(vals[i]);

        mAdapter = new MySimpleArrayAdapter(faActivity.getApplicationContext(),
                mListViewVals);

        try {
            mListView.setAdapter(mAdapter);
        }
        catch (NullPointerException ex){
            Log.e("MainActivity", ex.toString());
        }

        return rlLayout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(super.getActivity(), response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(mattnkev.cs.tufts.edu.musicmafia.EventPlaylistFragment.this);
                        mPlayer.addNotificationCallback(mattnkev.cs.tufts.edu.musicmafia.EventPlaylistFragment.this);
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
    public void onDestroy() {
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

    public void pauseButton(MenuItem mi) {
        // handle click here
        mPlayer.pause(null);
    }

    private void spotifySearch(final String query){

        final Activity activity = super.getActivity();
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
                        //mListViewVals = new String[entries.length()];
                        mListViewVals.clear();
                        for (int i = 0; i < entries.length(); i++) {
                            JSONObject entry = entries.getJSONObject(i);

                            String name = entry.getString("uri");
                            if(i==0){firstURI = name; }

                            name = entry.getString("name");
                            mListViewVals.add(name);
                        }
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.updateVals(mListViewVals);
                                mAdapter.notifyDataSetChanged();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        // Inflate the menu; this adds items to the action bar if it is present.
        super.getActivity().getMenuInflater().inflate(R.menu.menu_playlists, menu);

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
        });

    }

    private class MySimpleArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private String[] values;

        public MySimpleArrayAdapter(Context context, ArrayList<String> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values.toArray(new String [values.size()]);
        }

        public void updateVals(ArrayList<String> values) {
            this.values = values.toArray(new String [values.size()]);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_view_layout, parent, false);

            TextView songName = (TextView) rowView.findViewById(R.id.firstLine);
            TextView artistName = (TextView) rowView.findViewById(R.id.secondLine);
            TextView numberUpVotes = (TextView) rowView.findViewById(R.id.num_votes);

            songName.setText(values[position]);
            artistName.setText(values[position] + "Artist Name");

            ImageView upArrowImg = (ImageView) rowView.findViewById(R.id.up_arrow);
            ImageView downArrowImg = (ImageView) rowView.findViewById(R.id.down_arrow);

            upArrowImg.setImageResource(R.drawable.public_domain_up_arrow);
            downArrowImg.setImageResource(R.drawable.public_domain_down_arrow);

            upArrowImg.setOnClickListener(new MyClickListener(numberUpVotes, 1));
            downArrowImg.setOnClickListener(new MyClickListener(numberUpVotes, -1));

            return rowView;
        }
    }

    private class MyClickListener implements View.OnClickListener {
        private int delta_votes;
        private TextView num_votes;

        public MyClickListener(TextView num_votes, int delta_votes) {
            this.num_votes = num_votes;
            this.delta_votes = delta_votes;
        }

        public void onClick(View v) {
            int cur_votes = Integer.parseInt((String) num_votes.getText());
            cur_votes += delta_votes;
            num_votes.setText(String.valueOf(cur_votes));
            pushVoteToServer(delta_votes);
        }
    }

    private void pushVoteToServer(final int delta_votes){
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    JSONObject songData = new JSONObject();
                    songData.put("uri", "uri=spotify:track:3uulVrxiI7iLTjOBZsaiF8");//TODO: dont hard code
                    songData.put("val", delta_votes);

                    JSONObject postReqJSON = new JSONObject();
                    postReqJSON.put("EventName", "newplaylist");//TODO: dont hard code
                    postReqJSON.put("password", "dopepass");//TODO: dont hard code
                    postReqJSON.put("song", songData);
                    URL url = new URL("http://52.40.236.184:5000/vote");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");

                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(postReqJSON.toString());
                    wr.flush();

                    Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                    StringBuilder sb = new StringBuilder();
                    for (int c; (c = in.read()) >= 0;)
                        sb.append((char)c);
                    String response = sb.toString();
                    JSONObject data = new JSONObject(response);
                    //JSONObject resp = data.getJSONObject("Status");
                    //String loudScreaming = json.getJSONObject("LabelData").getString("slogan");
                    //String temp = data.getString("Status");
                    //if(data.getString("Status").equals("OK")){
                    //    changeActivity("Host");
                    //}
                    //JSONArray entries = tracks.getJSONArray("items");

                } catch (Exception ex){
                    Log.d("MainActivity", ex.toString());
                }
            }
        });
        thread.start();
    }

}
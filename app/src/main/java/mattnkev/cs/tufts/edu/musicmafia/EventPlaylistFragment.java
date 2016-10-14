package mattnkev.cs.tufts.edu.musicmafia;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.ConnectException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class EventPlaylistFragment extends Fragment
{
    private ListView mListView;
    private ArrayList<String> mListViewSongVals = new ArrayList<String>(), mListViewArtistVals = new ArrayList<String>();
    private MySimpleArrayAdapter mAdapter;
    private FragmentActivity faActivity;
    private final int PINGER_DELAY_SEC = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         faActivity  =  super.getActivity();
        RelativeLayout   rlLayout    = (RelativeLayout)    inflater.inflate(R.layout.event_playlist_layout, container, false);

        mListView = (ListView)rlLayout.findViewById(R.id.main_list_view);

        String[] vals = new String[] { "Android", "iPhoneReallyReallyReallyLongName", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
                "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
                "Android", "iPhone", "WindowsMobile" };
        vals = new String[20];
        for(int i=0; i<vals.length;i++) {
            mListViewSongVals.add("Song: "+vals[i]);
            mListViewArtistVals.add("ARTIST: " + vals[i]);
        }

        mAdapter = new MySimpleArrayAdapter(faActivity.getApplicationContext(),
                mListViewSongVals, mListViewArtistVals);

        try {
            mListView.setAdapter(mAdapter);
        }
        catch (NullPointerException ex){
            Log.e("MainActivity", ex.toString());
        }



        startPinger();

        return rlLayout;
    }

    public void addToListView(String[] songs, String[] artists, String[] URIs){
        mAdapter.updateVals(songs, artists, URIs);
        mAdapter.notifyDataSetChanged();
    }

//    public void addToListView(String song, String artist, String URI){
//        String[] songs = {song};String[] artists = {artist};String[] URIs = {URI};
//        mAdapter.updateVals(songs, artists, URIs);
//        mAdapter.notifyDataSetChanged();
//    }

    private class MySimpleArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private String[] songNames, artistNames, URIs;

        public String getURI(int position){
            return URIs[position];
        }

        public MySimpleArrayAdapter(Context context, ArrayList<String> songNames, ArrayList<String> artistNames) {
            super(context, -1, songNames);
            this.context = context;
            this.songNames = songNames.toArray(new String [songNames.size()]);
            this.artistNames = artistNames.toArray(new String [artistNames.size()]);
        }

        public void updateVals(String[] songNames, String[] artists, String[] uris){//ArrayList<String> songNames, ArrayList<String> artistNames) {
            this.songNames = songNames;//songNames.toArray(new String [songNames.size()]);
            this.artistNames = artists;//artistNames.toArray(new String [artistNames.size()]);
            this.URIs = uris;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_view_layout, parent, false);

            TextView songName = (TextView) rowView.findViewById(R.id.firstLine);
            TextView artistName = (TextView) rowView.findViewById(R.id.secondLine);
            TextView numberUpVotes = (TextView) rowView.findViewById(R.id.num_votes);

            songName.setText(songNames[position]);
            artistName.setText(artistNames[position]);

            ImageView upArrowImg = (ImageView) rowView.findViewById(R.id.up_arrow);
            ImageView downArrowImg = (ImageView) rowView.findViewById(R.id.down_arrow);

            upArrowImg.setImageResource(R.drawable.public_domain_up_arrow);
            downArrowImg.setImageResource(R.drawable.public_domain_down_arrow);

            upArrowImg.setOnClickListener(new MyClickListener(numberUpVotes, 1, position));
            downArrowImg.setOnClickListener(new MyClickListener(numberUpVotes, -1, position));

            return rowView;
        }
    }

    private class MyClickListener implements View.OnClickListener {
        private int delta_votes, position;
        private TextView num_votes;

        public MyClickListener(TextView num_votes, int delta_votes, int position) {
            this.num_votes = num_votes;
            this.delta_votes = delta_votes;
            this.position = position;
        }

        public void onClick(View v) {
            int cur_votes = Integer.parseInt((String) num_votes.getText());
            cur_votes += delta_votes;
            num_votes.setText(String.valueOf(cur_votes));
            pushVoteToServer(delta_votes, position);
        }
    }

    private void pushVoteToServer(final int delta_votes, final int position){
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    JSONObject songData = new JSONObject();
                    songData.put("uri", mAdapter.getURI(position));
                    songData.put("val", delta_votes);

                    JSONObject postReqJSON = new JSONObject();

                    Bundle extras = faActivity.getIntent().getExtras();
                    String eventName = "", password = "";
                    if (extras != null) {
                        eventName = extras.getString("EVENT_NAME");
                        password = extras.getString("PASSWORD");
                    }

                    postReqJSON.put("EventName", eventName);
                    postReqJSON.put("password", password);
                    postReqJSON.put("song", songData);
                    URL url = new URL("http://52.40.236.184:5000/vote");
                    conn = (HttpURLConnection)url.openConnection();
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
                    //do something with data response

                } catch (Exception ex){
                    Log.d("MainActivity", ex.toString());
                } finally {
                    if (conn != null)
                        conn.disconnect();
                }
            }
        });
        thread.start();
    }

    private void startPinger(){

        final Handler h = new Handler();
        final int delay = PINGER_DELAY_SEC*1000; //milliseconds

        //update immediately
        queryDatabase();
        //not needed?
        mAdapter.notifyDataSetChanged();
        h.postDelayed(new Runnable(){
            public void run(){
                queryDatabase();
                h.postDelayed(this, delay);
            }
        }, delay);
    }

    private void queryDatabase(){
        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Bundle extras = faActivity.getIntent().getExtras();
                String eventName = "", password = "";
                if (extras != null) {
                    eventName = extras.getString("EVENT_NAME");
                    password = extras.getString("PASSWORD");
                }
                HttpURLConnection conn = null;
                try {
                    String urlString = "http://52.40.236.184:5000/guestLogin"
                            +"?EventName="+eventName+"&password="+password;
                    URL url = new URL(urlString);
                    conn = (HttpURLConnection)url.openConnection();
                    InputStream in = conn.getInputStream();
                    StringBuilder sb = new StringBuilder();
                    for (int c; (c = in.read()) >= 0;)
                        sb.append((char)c);
                    String response = sb.toString();
                    JSONObject data = new JSONObject(response);
                    final String status = data.getString("Status");
                    JSONObject eventData = data.getJSONObject("Event");
                    if(status.equals("OK")){
                        //update playlist
                        JSONArray songsJson = eventData.getJSONArray("songs");
                        String[] songs = new String[20], artists = new String[20], uris = new String[20];
                        for(int i=0;i<songsJson.length();i++){
                            JSONObject songObj = songsJson.getJSONObject(i);
                            songs[i] = songObj.getString("name");
                            artists[i] = songObj.getString("artist");
                            uris[i] = songObj.getString("uri");

                        }

                        addToListView(songs, artists, uris);
                        //changeActivity("Guest", eventName, password);
                    } else {
                                /*runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mEventView.setError(status);
                                        mEventView.requestFocus();
                                    }
                                });*/
                        //TODO: nice error message
                    }
                } catch (ConnectException ex) {
                    //TODO: nice error message
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mEventView.setError("The server is down");
                                    mEventView.requestFocus();
                                }
                            });*/

                } catch (final Exception ex){
                    Log.d("MainActivity", ex.toString());
                    //TODO: nice error message
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mEventView.setError(ex.getMessage().toString());
                                    mEventView.requestFocus();
                                }
                            });*/

                } finally {
                    if (conn != null)
                        conn.disconnect();
                }
            }
        });
        thread.start();
    }

}
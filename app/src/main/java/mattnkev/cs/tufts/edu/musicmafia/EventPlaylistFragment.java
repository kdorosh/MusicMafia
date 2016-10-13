package mattnkev.cs.tufts.edu.musicmafia;

import android.content.Context;
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
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class EventPlaylistFragment extends Fragment
{
    private ListView mListView;
    private ArrayList<String> mListViewSongVals = new ArrayList<String>(), mListViewArtistVals = new ArrayList<String>();
    private MySimpleArrayAdapter mAdapter;
    private FragmentActivity faActivity;

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

        return rlLayout;
    }

//    public void addToListView(String[] songs, String[] artists, String[] URIs){
//        mAdapter.updateVals(songs, artists, URIs);
//        mAdapter.notifyDataSetChanged();
//    }

    public void addToListView(String song, String artist, String URI){
        String[] songs = {song};String[] artists = {artist};String[] URIs = {URI};
        mAdapter.updateVals(songs, artists, URIs);
        mAdapter.notifyDataSetChanged();
    }

    private class MySimpleArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private String[] songNames, artistNames, URIs;

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
                HttpURLConnection conn = null;
                try {
                    JSONObject songData = new JSONObject();
                    songData.put("uri", "spotify:track:3uulVrxiI7iLTjOBZsaiF8");//TODO: dont hard code
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

}
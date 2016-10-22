package mattnkev.cs.tufts.edu.musicmafia.fragments;

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
import java.util.ArrayList;

import mattnkev.cs.tufts.edu.musicmafia.R;
import mattnkev.cs.tufts.edu.musicmafia.Utils;


public class EventPlaylistFragment extends Fragment
{
    private ListView mListView;
    private ArrayList<String> mListViewSongVals = new ArrayList<String>(), mListViewArtistVals = new ArrayList<String>();
    private MySimpleArrayAdapter mAdapter;
    private FragmentActivity faActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        faActivity = super.getActivity();
        RelativeLayout rlLayout = (RelativeLayout)inflater.inflate(R.layout.fragment_event_playlist, container, false);

        mListView = (ListView)rlLayout.findViewById(R.id.main_list_view);

        String[] vals = new String[Utils.MAX_LISTVIEW_LEN];
        for(int i = 0; i < vals.length; i++) {
            mListViewSongVals.add("Song: " + vals[i]);
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

        public void updateVals(String[] songNames, String[] artists, String[] uris){
            this.songNames = songNames;
            this.artistNames = artists;
            this.URIs = uris;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_view_layout_event_playlist, parent, false);

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

                try {
                    Bundle extras = faActivity.getIntent().getExtras();
                    String eventName = "", password = "";
                    if (extras != null) {
                        eventName = extras.getString("EVENT_NAME");
                        password = extras.getString("PASSWORD");
                    }

                    JSONObject songData = new JSONObject();
                    songData.put("uri", mAdapter.getURI(position));
                    songData.put("val", delta_votes);

                    Utils.attemptPOST(Utils.SERVER_URL, "vote",
                            new String[]{"EventName", "password", "song"},
                            new String[]{eventName, password, songData.toString()});
                }
                catch (Exception ex) {}

            }
        });
        thread.start();
    }

    private void startPinger(){

        final Handler h = new Handler();
        final int delay = Utils.PINGER_DELAY_SEC * 1000; //milliseconds

        //update immediately
        queryDatabase();
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

                String resp = Utils.attemptGET(Utils.SERVER_URL, "guestLogin",
                        new String[] {"EventName", "password"},
                        new String[] {eventName, password});

                try {
                    JSONObject data = new JSONObject(resp);
                    JSONObject eventData = data.getJSONObject("Event");
                    if (data.getString("Status").equals("OK")) {
                        //update playlist
                        JSONArray songsJson = eventData.getJSONArray("songs");
                        String[] songs = new String[Utils.MAX_LISTVIEW_LEN],
                                 artists = new String[Utils.MAX_LISTVIEW_LEN],
                                 uris = new String[Utils.MAX_LISTVIEW_LEN];
                        for (int i = 0; i < songsJson.length(); i++) {
                            JSONObject songObj = songsJson.getJSONObject(i);
                            songs[i] = songObj.getString("name");
                            artists[i] = songObj.getString("artist");
                            uris[i] = songObj.getString("uri");
                        }
                        addToListView(songs, artists, uris);
                    }
                }
                catch (Exception ex) {}

            }
        });
        thread.start();
    }

}
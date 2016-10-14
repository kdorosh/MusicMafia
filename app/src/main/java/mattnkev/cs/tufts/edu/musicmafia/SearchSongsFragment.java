package mattnkev.cs.tufts.edu.musicmafia;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class SearchSongsFragment extends Fragment {

    private ListView mListView;
    private ArrayList<String> mListViewSongVals = new ArrayList<String>(), mListViewArtistVals = new ArrayList<String>();
    private MySimpleArrayAdapter mAdapter;
    private FragmentActivity faActivity;
    private SearchView mSearchView;
    private MenuItem menuItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        faActivity = super.getActivity();
        setHasOptionsMenu(true);
        RelativeLayout rlLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_search_song, container, false);

        mSearchView = (SearchView) rlLayout.findViewById(R.id.spotify_song_search);
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                ((PlaylistMaking)faActivity).spotifySearch(query);
                mSearchView.clearFocus();
                return true;
            }
        };

        mSearchView.setOnQueryTextListener(queryTextListener);
        /*mSearchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (mOnQueryTextFocusChangeListener != null) {
                    mOnQueryTextFocusChangeListener.onFocusChange(SearchView.this, hasFocus);
                }
                if (hasFocus) {
                    ((PlaylistMaking)faActivity).setBottomBarVisibility(false);
                } else {
                    ((PlaylistMaking)faActivity).setBottomBarVisibility(true);
                }
            }
        });*/
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(!queryTextFocused) {
                    ((PlaylistMaking)faActivity).setBottomBarVisibility(true);
                    //mSearchView.setQuery("", false);
                } else {
                    ((PlaylistMaking)faActivity).setBottomBarVisibility(false);
                }
            }
        });


        mListView = (ListView) rlLayout.findViewById(R.id.main_list_view);

        String[] vals = new String[]{"Android", "iPhoneReallyReallyReallyLongName", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
                "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
                "Android", "iPhone", "WindowsMobile"};
        int MAX_SIZE=20;
        vals = new String[MAX_SIZE];
        for (int i = 0; i < vals.length; i++) {
            mListViewSongVals.add(vals[i] + "2");
            mListViewArtistVals.add("Artist: " +vals[i] + "2");
        }

        mAdapter = new MySimpleArrayAdapter(faActivity.getApplicationContext(),
                mListViewSongVals, mListViewArtistVals);

        try {
            mListView.setAdapter(mAdapter);
        } catch (NullPointerException ex) {
            Log.e("MainActivity", ex.toString());
        }

        return rlLayout;
    }


    public void updateListView(String[] songs, String[] artists, String[] URIs){
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
            this.URIs = new String[20];
        }

        public void updateVals(String[] songNames, String[] artistNames, String[] URIs) {
            this.songNames = songNames; //songNames.toArray(new String [songNames.size()]);
            this.artistNames = artistNames;//artistNames.toArray(new String [artistNames.size()]);
            this.URIs = URIs;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_view_layout_search, parent, false);

            TextView songName = (TextView) rowView.findViewById(R.id.firstLine);
            TextView artistName = (TextView) rowView.findViewById(R.id.secondLine);

            songName.setText(songNames[position]);
            artistName.setText(artistNames[position]);

            ImageView plusIcon = (ImageView) rowView.findViewById(R.id.plus_icon);
            plusIcon.setImageResource(R.drawable.public_domain_plus);
            plusIcon.setOnClickListener(new MyClickListener(songNames[position], artistNames[position], URIs[position]));

            return rowView;
        }
    }

    private class MyClickListener implements View.OnClickListener {
        //private int delta_votes;
        //private TextView num_votes;
        private String songName, artistName, URI;

        public MyClickListener(String songName, String artistName, String uri){//TextView num_votes, int delta_votes) {
            //this.num_votes = num_votes;
            //this.delta_votes = delta_votes;
            this.songName = songName;
            this.artistName = artistName;
            this.URI = uri;
        }

        public void onClick(View v) {
            addSongToServerPlaylist(songName, artistName, URI);
        }
    }

    private void addSongToServerPlaylist(final String songName, final String artistName, final String uri){
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    JSONObject songData = new JSONObject();
                    songData.put("name", songName);
                    songData.put("artist", artistName);
                    songData.put("uri", uri);

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
                    URL url = new URL("http://52.40.236.184:5000/addSong");
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
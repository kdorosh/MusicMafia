package mattnkev.cs.tufts.edu.musicmafia.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.ArrayList;
import mattnkev.cs.tufts.edu.musicmafia.R;
import mattnkev.cs.tufts.edu.musicmafia.Utils;
import mattnkev.cs.tufts.edu.musicmafia.activities.PlaylistMakingActivity;

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

            public boolean onQueryTextSubmit(final String query) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String resp = Utils.attemptGET(Utils.SPOTIFY_SERVER_URL, "search",
                                new String[]{"q", "type", "market"},
                                new String[]{query, "track", "US"});
                        final Utils.SpotifyResp spotifyResp = Utils.parseSpotifyResp(resp);

                        if (spotifyResp != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    updateListView(spotifyResp.getSearchListViewSongs(),
                                            spotifyResp.getSearchListViewSongs(),
                                            spotifyResp.getSearchListViewURIs());

                                    mSearchView.clearFocus();
                                }
                            });
                        }

                    }
                });
                thread.start();

                return true;
            }
        };

        mSearchView.setOnQueryTextListener(queryTextListener);
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(queryTextFocused) {
                    ((PlaylistMakingActivity) faActivity).setBottomBarVisibility(false);
                } else {
                    ((PlaylistMakingActivity)faActivity).setBottomBarVisibility(true);
                    mSearchView.setQuery("", false);
                }
            }
        });


        mListView = (ListView) rlLayout.findViewById(R.id.main_list_view);


        String[] vals = new String[Utils.MAX_LISTVIEW_LEN];
        for (String val : vals) {
            mListViewSongVals.add(val + "2");
            mListViewArtistVals.add("Artist: " + val + "2");
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

    public void updateListView(final String[] songs, final String[] artists, final String[] URIs){
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
            this.songNames = songNames;
            this.artistNames = artistNames;
            this.URIs = URIs;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_view_layout_search_song, parent, false);

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
        private String songName, artistName, URI;

        public MyClickListener(String songName, String artistName, String uri){//TextView num_votes, int delta_votes) {
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

                Bundle extras = faActivity.getIntent().getExtras();
                String eventName = "", password = "";
                if (extras != null) {
                    eventName = extras.getString("EVENT_NAME");
                    password = extras.getString("PASSWORD");
                }
                JSONObject songData = new JSONObject();
                try
                {
                    songData.put("name", songName);
                    songData.put("artist", artistName);
                    songData.put("uri", uri);
                }
                catch (Exception ex) { Log.d("SearchSongsFragment", ex.toString()); }

                String response = Utils.attemptPOST(Utils.SERVER_URL, "addSong",
                        new String[] {"EventName", "password", "song"},
                        new String[] {eventName, password, songData.toString()});

            }
        });
        thread.start();
    }
}
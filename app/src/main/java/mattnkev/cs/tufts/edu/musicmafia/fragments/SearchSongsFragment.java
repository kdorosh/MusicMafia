package mattnkev.cs.tufts.edu.musicmafia.fragments;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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

    private final ArrayList<String> mListViewSongValues = new ArrayList<>(), mListViewArtistValues = new ArrayList<>();
    private MySimpleArrayAdapter mAdapter;
    private FragmentActivity faActivity;
    private SearchView mSearchView;

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

                        if (spotifyResp != null && faActivity != null) {
                            faActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    updateListView(spotifyResp.getSearchListViewSongs(),
                                            spotifyResp.getSearchListViewArtists(),
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


        ListView listView = (ListView) rlLayout.findViewById(R.id.main_list_view);


        String[] values = new String[Utils.MAX_LISTVIEW_LEN];
        for (int c = 0; c < Utils.MAX_LISTVIEW_LEN; c++)
            values[c] = "placeholder"+c;
        for (String val : values) {
            mListViewSongValues.add(val);
            mListViewArtistValues.add("Artist: " + val);
        }

        mAdapter = new MySimpleArrayAdapter(faActivity.getApplicationContext(),
                mListViewSongValues, mListViewArtistValues);

        try {
            listView.setAdapter(mAdapter);
        } catch (NullPointerException ex) {
            Log.e("MainActivity", ex.toString());
        }

        return rlLayout;
    }

    private void updateListView(final String[] songs, final String[] artists, final String[] URIs){
        //mAdapter.clear();
        for (String song : songs)
        {
            //mAdapter.insert(song, mAdapter.getCount());
        }

        mAdapter.updateValues(songs, artists, URIs);
        mAdapter.notifyDataSetChanged();
    }

    private class MySimpleArrayAdapter extends BaseAdapter {
        private final Context context;
        private String[] songNames, artistNames, URIs;

        private MySimpleArrayAdapter(Context context, ArrayList<String> songNames, ArrayList<String> artistNames) {
            super();
            //super(context, -1, songNames);
            this.context = context;
            this.songNames = songNames.toArray(new String [songNames.size()]);
            this.artistNames = artistNames.toArray(new String [artistNames.size()]);
            this.URIs = new String[20];
        }

        private void updateValues(String[] songNames, String[] artistNames, String[] URIs) {
            this.songNames = songNames;
            this.artistNames = artistNames;
            this.URIs = URIs;
        }

        @Override
        public int getCount() {
            return songNames.length;
        }

        @Override
        public Object getItem(int position) {
            return songNames[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_view_layout_search_song, parent, false);

            TextView songName = (TextView) convertView.findViewById(R.id.firstLine);
            TextView artistName = (TextView) convertView.findViewById(R.id.secondLine);

            songName.setText(songNames[position]);
            artistName.setText(artistNames[position]);

            ImageView plusIcon = (ImageView) convertView.findViewById(R.id.plus_icon);
            plusIcon.setImageResource(R.drawable.public_domain_plus);
            plusIcon.setOnClickListener(new MyClickListener(songNames[position], artistNames[position], URIs[position]));


            return convertView;
        }
    }

    private class MyClickListener implements View.OnClickListener {
        private final String songName, artistName, URI;

        private MyClickListener(String songName, String artistName, String uri){
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
                catch (Exception ex) { Log.e("SearchSongsFragment", ex.toString()); }

                String status = Utils.attemptPOST("addSong",
                        new String[] {"EventName", "password", "song"},
                        new String[] {eventName, password, songData.toString()});

                //TODO: error message if status is bad
                boolean dummy = status.equals("OK");

            }
        });
        thread.start();
    }
}
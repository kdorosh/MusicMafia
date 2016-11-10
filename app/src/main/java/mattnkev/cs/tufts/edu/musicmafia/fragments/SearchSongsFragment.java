package mattnkev.cs.tufts.edu.musicmafia.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import org.json.JSONObject;

import java.util.List;

import mattnkev.cs.tufts.edu.musicmafia.R;
import mattnkev.cs.tufts.edu.musicmafia.SongData;
import mattnkev.cs.tufts.edu.musicmafia.Utils;
import mattnkev.cs.tufts.edu.musicmafia.activities.PlaylistMakingActivity;

public class SearchSongsFragment extends Fragment {

    private SearchSongsAdapter mAdapter;
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
                                null, null, //eventName and password not needed for spotify query
                                new String[]{"q", "type", "market"},
                                new String[]{query, "track", "US"});
                        final Utils.PlaylistData spotifyResp = Utils.parseSpotifyResp(resp);

                        if (spotifyResp != null && faActivity != null) {
                            faActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    updateListView(spotifyResp.getSongDatas());
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
                    mAdapter.resetBackgroundColors();
                }
            }
        });


        ListView listView = (ListView) rlLayout.findViewById(R.id.main_list_view);


        mAdapter = new SearchSongsAdapter(faActivity.getApplicationContext(),
                new SongData[0]);

        if (listView != null)
            listView.setAdapter(mAdapter);


        return rlLayout;
    }

    private void updateListView(final SongData[] songsData){
        mAdapter.updateValues(songsData);
        mAdapter.notifyDataSetChanged();
    }

    private class SearchSongsAdapter extends BaseAdapter {
        private final Context context;
        private SongData[] songsData;
        private int[] colors;

        private SearchSongsAdapter(Context context, SongData[] songs) {
            super();
            this.context = context;
            this.songsData = songs;
            this.colors = new int[songs.length];
            resetBackgroundColors();
        }

        private void updateValues(SongData[] songs) {
            this.songsData = songs;
            this.colors = new int[songs.length];
        }

        private void setBackgroundColor(int position, int c) {
            colors[position] = c;
        }

        private void resetBackgroundColors() {
            for (int i = 0; i < colors.length; i++)
                colors[i] = ContextCompat.getColor(context, R.color.bb_darkBackgroundColor);
        }

        @Override
        public int getCount() {
            return songsData.length;
        }

        @Override
        public Object getItem(int position) {
            return songsData[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView==null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_view_layout_search_song, parent, false);
            }
            convertView.setBackgroundColor(Color.BLACK);
            TextView songName = (TextView) convertView.findViewById(R.id.firstLine);
            TextView artistName = (TextView) convertView.findViewById(R.id.secondLine);
            songName.setTextColor(Color.WHITE);
            songName.setTypeface(null, Typeface.BOLD);
            artistName.setTextColor(Color.WHITE);
            songName.setText(songsData[position].getSongName());
            artistName.setText(songsData[position].getArtist());

            ImageView plusIcon = (ImageView) convertView.findViewById(R.id.plus_icon);
            plusIcon.setImageResource(R.drawable.public_domain_plus);
            plusIcon.setOnClickListener(new PlusClickListener(songsData[position], position));
            convertView.setBackgroundColor(colors[position]);

            return convertView;
        }
    }

    private class PlusClickListener implements View.OnClickListener {
        private final SongData songData;
        private final int position;

        private PlusClickListener(SongData song, int pos){
            this.songData = song;
            this.position = pos;
        }

        public void onClick(View v) {
            addSongToServerPlaylist(songData, position);
        }
    }

    private void addSongToServerPlaylist(final SongData song, final int position){
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                Utils.EventData eventData = new Utils.EventData(faActivity);

                JSONObject songData = new JSONObject();
                try
                {
                    songData.put("name", song.getSongName());
                    songData.put("artist", song.getArtist());
                    songData.put("uri", song.getURI());
                    songData.put("ms_duration", String.valueOf(song.getDuration()));
                    songData.put("album_art", song.getAlbumArt());
                }
                catch (Exception ex) { Log.e("SearchSongsFragment", ex.toString()); }

                faActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        mAdapter.setBackgroundColor(position,
                                ContextCompat.getColor(faActivity.getApplicationContext(), R.color.bb_tabletRightBorderDark));
                        mAdapter.notifyDataSetChanged();
                    }
                });

                String status = Utils.attemptPOST("addSong",
                        eventData.getEventName(), eventData.getPassword(),
                        new String[] {"song"},
                        new String[] {songData.toString()});

                status = Utils.parseRespForStatus(status);

                final int color = status.equals("OK") ? R.color.green : R.color.red;
                faActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        mAdapter.setBackgroundColor(position,
                                ContextCompat.getColor(faActivity.getApplicationContext(), color));
                        mAdapter.notifyDataSetChanged();
                    }
                });

                if (!status.equals("OK")) { Utils.displayMsg(faActivity, status); }

                // update playlist manually after song added
                List<Fragment> fragments = ((PlaylistMakingActivity) faActivity).getFragments();
                for (Fragment fragment : fragments) {
                    if (fragment instanceof EventPlaylistFragment) {
                            ((EventPlaylistFragment) fragment).queryDatabase();
                    }
                }

            }
        });
        thread.start();
    }
}
package mattnkev.cs.tufts.edu.musicmafia.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import mattnkev.cs.tufts.edu.musicmafia.R;
import mattnkev.cs.tufts.edu.musicmafia.Utils;
import mattnkev.cs.tufts.edu.musicmafia.activities.PlaylistMakingActivity;

/**
 * Created by Kevin on 11/5/2016.
 */

public class NowPlayingFragment extends Fragment {

    private FragmentActivity faActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        faActivity = super.getActivity();

        setHasOptionsMenu(true);
        RelativeLayout rlLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_search_song, container, false);

        /*mSearchView = (SearchView) rlLayout.findViewById(R.id.spotify_song_search);
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
                                    updateListView(spotifyResp.getSongs(),
                                            spotifyResp.getArtists(),
                                            spotifyResp.getURIs());

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


        mAdapter = new SearchSongsFragment.SearchSongsAdapter(faActivity.getApplicationContext(),
                new String[0], new String[0], new String[0], new int[0]);

        if (listView != null)
            listView.setAdapter(mAdapter);*/
        return rlLayout;
    }
}

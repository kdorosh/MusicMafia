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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class SearchSongsFragment extends Fragment {
    //private static final String CLIENT_ID = "fe81735360154ee7921c12078d656a97";
    //private static final String REDIRECT_URI = "android-app-login://callback";

    //private Player mPlayer;
    private ListView mListView;
    private ArrayList<String> mListViewVals = new ArrayList<String>();
    private MySimpleArrayAdapter mAdapter;

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity = super.getActivity();
        RelativeLayout rlLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_search_song, container, false);

        mListView = (ListView) rlLayout.findViewById(R.id.main_list_view);

        String[] vals = new String[]{"Android", "iPhoneReallyReallyReallyLongName", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
                "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
                "Android", "iPhone", "WindowsMobile"};
        for (int i = 0; i < vals.length; i++)
            mListViewVals.add(vals[i]+"2");

        mAdapter = new MySimpleArrayAdapter(faActivity.getApplicationContext(),
                mListViewVals);

        try {
            mListView.setAdapter(mAdapter);
        } catch (NullPointerException ex) {
            Log.e("MainActivity", ex.toString());
        }

//        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
//        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                spotifySearch(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });

        return rlLayout;
    }

    private class MySimpleArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private String[] values;

        public MySimpleArrayAdapter(Context context, ArrayList<String> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values.toArray(new String[values.size()]);
        }

        public void updateVals(ArrayList<String> values) {
            this.values = values.toArray(new String[values.size()]);
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

            //upArrowImg.setOnClickListener(new MyClickListener(numberUpVotes, 1));
            //downArrowImg.setOnClickListener(new MyClickListener(numberUpVotes, -1));

            return rowView;
        }
    }
}
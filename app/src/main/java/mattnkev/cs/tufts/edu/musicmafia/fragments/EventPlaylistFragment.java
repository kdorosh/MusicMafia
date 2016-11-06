package mattnkev.cs.tufts.edu.musicmafia.fragments;

import android.content.Context;
import android.os.Handler;
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
import android.widget.TextView;
import org.json.JSONObject;

import mattnkev.cs.tufts.edu.musicmafia.R;
import mattnkev.cs.tufts.edu.musicmafia.Utils;
import mattnkev.cs.tufts.edu.musicmafia.activities.PlaylistMakingActivity;

public class EventPlaylistFragment extends Fragment
{
    private EventPlaylistAdapter mAdapter;
    private FragmentActivity faActivity;
    private enum VOTE_STATE {NO_VOTE, UP_VOTE, DOWN_VOTE}
    private VOTE_STATE[] cachedVoteStates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        faActivity = super.getActivity();
        RelativeLayout rlLayout = (RelativeLayout)inflater.inflate(R.layout.fragment_event_playlist, container, false);

        ListView listView = (ListView)rlLayout.findViewById(R.id.main_list_view);

        mAdapter = new EventPlaylistAdapter(faActivity.getApplicationContext(),
                new String[0], new String[0], new String[0]);

        if (listView != null)
            listView.setAdapter(mAdapter);

        startPinger();

        return rlLayout;
    }

    public String getSongUri(int position){
        if (mAdapter.URIs != null)
            return position < mAdapter.URIs.length ? mAdapter.getURI(position) : null;
        return null;
    }

    public int getDuration(int position) {
        if (mAdapter.mDurations != null)
            return position < mAdapter.mDurations.length ? mAdapter.getDuration(position) : 0;
        return 0;
    }

    public String getAlbumArt(int position){
        if (mAdapter.albums != null)
            return position < mAdapter.albums.length ? mAdapter.getAlbum(position) : null;
        return null;
    }

    public String getSongName(int position){
        if (mAdapter.songNames != null)
            return position < mAdapter.songNames.length ? mAdapter.getSong(position) : null;
        return null;
    }

    private void updateListView(final Utils.PlaylistData playlistData){
        faActivity.runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.updateValues(playlistData.getSongs(),
                        playlistData.getArtists(),
                        playlistData.getURIs(),
                        playlistData.getAlbumArts(),
                        playlistData.getDurations(),
                        playlistData.getVotes());
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private class EventPlaylistAdapter extends BaseAdapter {
        private final Context context;
        private String[] songNames, artistNames, URIs, albums;
        private int[] colors, mVotes, mDurations;

        private String getURI(int position){
            return URIs[position];
        }
        private String getAlbum(int position) { return albums[position]; }
        private String getSong(int position) { return songNames[position]; }
        private int getDuration(int position){
            return mDurations[position];
        }

        private EventPlaylistAdapter(Context context, String[] songs, String[] artists, String[] uris) {
            super();
            this.context = context;
            this.songNames = songs;
            this.artistNames = artists;
            this.URIs = uris;
            this.albums = new String[songNames.length];
            this.colors = new int[songNames.length];
            resetBackgroundColors();
            this.mVotes = new int[songNames.length];
            this.mDurations = new int[songNames.length];
        }

        private void updateValues(final String[] songNames, final String[] artists, final String[] uris,
                                  final String[] albumArts, final int[] durations, final int[] votes){
            updateCache(this.URIs, uris);
            this.songNames = songNames;
            this.artistNames = artists;
            this.URIs = uris;
            this.albums = albumArts;
            this.mDurations = durations;
            this.mVotes = votes;
            this.colors = new int[songNames.length];
            resetBackgroundColors();
        }

        private void setBackgroundColor(int position, int c) {
            colors[position] = c;
        }

        private void resetBackgroundColors(){
            for (int i = 0; i < colors.length; i++) {
                int color;
                if (cachedVoteStates[i] == VOTE_STATE.UP_VOTE)
                    color = R.color.green;
                else if (cachedVoteStates[i] == VOTE_STATE.DOWN_VOTE)
                    color = R.color.red;
                else
                    color = R.color.bb_darkBackgroundColor;
                colors[i] = ContextCompat.getColor(context, color);
            }
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
            if (convertView == null) {

                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_view_layout_event_playlist, parent, false);
            }

            TextView songName = (TextView) convertView.findViewById(R.id.firstLine);
            TextView artistName = (TextView) convertView.findViewById(R.id.secondLine);
            TextView numberUpVotes = (TextView) convertView.findViewById(R.id.num_votes);

            songName.setText(songNames[position]);
            artistName.setText(artistNames[position]);
            numberUpVotes.setText(String.valueOf(mVotes[position]));

            ImageView upArrowImg = (ImageView) convertView.findViewById(R.id.up_arrow);
            ImageView downArrowImg = (ImageView) convertView.findViewById(R.id.down_arrow);

            upArrowImg.setImageResource(R.drawable.public_domain_up_arrow);
            downArrowImg.setImageResource(R.drawable.public_domain_down_arrow);

            upArrowImg.setOnClickListener(new VoteClickListener(1, position));
            downArrowImg.setOnClickListener(new VoteClickListener(-1, position));

            convertView.setBackgroundColor(colors[position]);

            return convertView;
        }
    }

    private class VoteClickListener implements View.OnClickListener {
        private final int delta_votes, position;

        private VoteClickListener(int delta_votes, int position) {
            this.delta_votes = delta_votes;
            this.position = position;
        }

        public void onClick(View v) {
            if (cachedVoteStates[position] == VOTE_STATE.NO_VOTE ||
                    cachedVoteStates[position] == VOTE_STATE.UP_VOTE && delta_votes == -1 ||
                    cachedVoteStates[position] == VOTE_STATE.DOWN_VOTE && delta_votes == 1) {

                // changing vote from previous value, double delta val to counteract previous vote
                int change_votes_val = delta_votes;
                if(cachedVoteStates[position] != VOTE_STATE.NO_VOTE)
                    change_votes_val = delta_votes * 2;

                mAdapter.mVotes[position] += change_votes_val;
                pushVoteToServer(change_votes_val, position);
            }
            else {
                Utils.displayMsg(faActivity, "Cannot vote twice the same way");
            }
        }
    }

    // Assumes there are no duplicate URIs in the cache
    private void updateCache(String[] origUris, String[] newUris) {
        VOTE_STATE[] newCache = new VOTE_STATE[newUris.length];

        // initialize to NO_VOTE
        for (int c = 0; c < newCache.length; c++)
            newCache[c] = VOTE_STATE.NO_VOTE;

        // for each URI in old data
        for (int origIndex = 0; origIndex < origUris.length; origIndex++) {
            // for each URI in new data
            for (int newIndex = 0; newIndex < newUris.length; newIndex++) {
                // if found, carry over voting cache data
                // else, enable voting
                if (newUris[newIndex].equals(origUris[origIndex]) &&
                        origIndex < cachedVoteStates.length &&  cachedVoteStates[origIndex] != null) {
                    newCache[newIndex] = cachedVoteStates[origIndex];
                }
            }
        }
        cachedVoteStates = newCache;
    }

    private void pushVoteToServer(final int delta_votes, final int position){
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    Utils.EventData eventData = new Utils.EventData(faActivity);

                    JSONObject songData = new JSONObject();
                    songData.put("uri", mAdapter.getURI(position));
                    songData.put("val", delta_votes);

                    faActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            mAdapter.setBackgroundColor(position,
                                    ContextCompat.getColor(faActivity.getApplicationContext(), R.color.bb_tabletRightBorderDark));
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                    final String resp = Utils.attemptPOST("vote",
                            eventData.getEventName(), eventData.getPassword(),
                            new String[]{"song"},
                            new String[]{songData.toString()});

                    final String status = Utils.parseRespForStatus(resp);
                    faActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            final int color = delta_votes == 1 ? R.color.green : R.color.red;
                            if (status.equals("OK")) {

                                // we've voted! cache value so we cannot vote again
                                VOTE_STATE vote_state = delta_votes == 1 ? VOTE_STATE.UP_VOTE : VOTE_STATE.DOWN_VOTE;
                                cachedVoteStates[position] = vote_state;

                                mAdapter.setBackgroundColor(position, ContextCompat.getColor(faActivity.getApplicationContext(), color));
                                mAdapter.notifyDataSetChanged();
                            } else {
                                Utils.displayMsg(faActivity, resp);
                            }
                        }
                    });
                }
                catch (Exception ex) {
                    Utils.displayMsg(faActivity, ex.toString());
                    Log.e("pushVoteToServer", ex.toString());
                }

            }
        });
        thread.start();
    }

    private void startPinger(){

        final Handler h = new Handler();
        final int delay = Utils.PINGER_DELAY_SEC * 1000; //milliseconds

        //update immediately
        queryDatabase();
        h.postDelayed(new Runnable(){
            public void run(){
                queryDatabase();
                h.postDelayed(this, delay);
            }
        }, delay);
    }

    public void queryDatabase(){
        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                Utils.EventData eventData = new Utils.EventData(faActivity);

                String resp = Utils.attemptGET(Utils.SERVER_URL, "guestLogin",
                        eventData.getEventName(), eventData.getPassword(),
                        new String[] {},
                        new String[] {});

                Utils.PlaylistData playlistData = Utils.parseCurrentPlaylist(resp, faActivity);
                if (playlistData != null)
                    updateListView(playlistData);

                faActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        ((PlaylistMakingActivity)faActivity).updateAlbumArt();                    }
                });


            }
        });
        thread.start();
    }



}
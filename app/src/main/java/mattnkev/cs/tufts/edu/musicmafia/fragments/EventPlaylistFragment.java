package mattnkev.cs.tufts.edu.musicmafia.fragments;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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

import java.util.List;

import mattnkev.cs.tufts.edu.musicmafia.R;
import mattnkev.cs.tufts.edu.musicmafia.SongData;
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

        mAdapter = new EventPlaylistAdapter(faActivity.getApplicationContext(), new SongData[0]);

        if (listView != null)
            listView.setAdapter(mAdapter);

        startPinger();

        return rlLayout;
    }

    public SongData getSongsData(int position){
        if (mAdapter.songsData != null)
            return position < mAdapter.songsData.length ? mAdapter.songsData[position] : null;
        return null;
    }

    private void updateListView(final Utils.PlaylistData playlistData){
        faActivity.runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.updateValues(playlistData.getSongDatas());
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private class EventPlaylistAdapter extends BaseAdapter {
        private final Context context;
        private SongData[] songsData;
        private int[] colors;

        private SongData getSongsData(int position){
            return songsData[position];
        }

        private EventPlaylistAdapter(Context context, SongData[] songs) {
            super();
            this.context = context;
            this.songsData = songs;
            this.colors = new int[songs.length];
            resetBackgroundColors();
        }

        private void updateValues(final SongData[] songs){
            updateCache(this.songsData, songs);
            this.songsData = songs;
            this.colors = new int[songs.length];
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
            if (convertView == null) {

                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_view_layout_event_playlist, parent, false);
            }
            convertView.setBackgroundColor(Color.BLACK);
            TextView songName = (TextView) convertView.findViewById(R.id.firstLine);
            TextView artistName = (TextView) convertView.findViewById(R.id.secondLine);
            TextView numberUpVotes = (TextView) convertView.findViewById(R.id.num_votes);
            numberUpVotes.setTextColor(Color.WHITE);
            songName.setText(songsData[position].getSongName());
            artistName.setText(songsData[position].getArtist());
            numberUpVotes.setText(String.valueOf(songsData[position].getVotes()));
            songName.setTextColor(Color.WHITE);
            artistName.setTextColor(Color.WHITE);
            songName.setTypeface(null, Typeface.BOLD);

            ImageView upArrowImg = (ImageView) convertView.findViewById(R.id.up_arrow);
            ImageView downArrowImg = (ImageView) convertView.findViewById(R.id.down_arrow);

            upArrowImg.getLayoutParams().width = 120;
            upArrowImg.getLayoutParams().height = 120;
            downArrowImg.getLayoutParams().width = 120;
            downArrowImg.getLayoutParams().height = 120;

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

                int newVotes = mAdapter.getSongsData(position).getVotes() + change_votes_val;
                mAdapter.getSongsData(position).setVotes(newVotes);
                pushVoteToServer(change_votes_val, position);
            }
            else {
                Utils.displayMsg(faActivity, "Cannot vote twice the same way");
            }
        }
    }

    // Assumes there are no duplicate URIs in the cache
    private void updateCache(SongData[] origData, SongData[] newData) {
        VOTE_STATE[] newCache = new VOTE_STATE[newData.length];

        // initialize to NO_VOTE
        for (int c = 0; c < newCache.length; c++)
            newCache[c] = VOTE_STATE.NO_VOTE;

        // for each URI in old data
        for (int origIndex = 0; origIndex < origData.length; origIndex++) {
            // for each URI in new data
            for (int newIndex = 0; newIndex < newData.length; newIndex++) {
                // if found, carry over voting cache data
                // else, enable voting
                if (newData[newIndex].getURI().equals(origData[origIndex].getURI()) &&
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
                    songData.put("uri", mAdapter.getSongsData(position).getURI());
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

                final Utils.PlaylistData playlistData = Utils.parseCurrentPlaylist(resp, faActivity);
                if (playlistData != null) {
                    updateListView(playlistData);

                    // update NowPlayingFragment with new contents from pinger query
                    faActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            List<Fragment> fragments = ((PlaylistMakingActivity) faActivity).getFragments();
                            for (Fragment fragment : fragments) {
                                if (fragment instanceof NowPlayingFragment) {
                                    SongData[] songsData = playlistData.getSongDatas();
                                    if (songsData != null && songsData.length > 0)
                                        ((NowPlayingFragment) fragment).updateCurrentSong(songsData[0]);
                                }
                            }
                        }
                    });
                }


            }
        });
        thread.start();
    }



}
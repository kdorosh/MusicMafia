package mattnkev.cs.tufts.edu.musicmafia.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import mattnkev.cs.tufts.edu.musicmafia.CustomVolleyRequestQueue;
import mattnkev.cs.tufts.edu.musicmafia.R;
import mattnkev.cs.tufts.edu.musicmafia.SongData;
import mattnkev.cs.tufts.edu.musicmafia.Utils;
import mattnkev.cs.tufts.edu.musicmafia.activities.PlaylistMakingActivity;

import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * Created by Kevin on 11/5/2016.
 *
 * https://www.tutorialspoint.com/android/android_mediaplayer.htm
 */

public class NowPlayingFragment extends Fragment {

    private FragmentActivity faActivity;

    private RelativeLayout mRLayout;
    private ImageLoader mImageLoader;

    private double startTime = 0;
    private double finalTime = 0;

    private final Handler myHandler = new Handler();
    private SeekBar seekbar;
    private TextView tVcurrentTime, tVfinalTime, tVsongName;
    private Button playPause;
    private static int oneTimeOnly = 0;
    private static boolean currentlySeeking = false;
    private String mCurrentUri;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        faActivity = super.getActivity();

        setHasOptionsMenu(true);
        mRLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_now_playing, container, false);

        Button backButton, skipButton;
        backButton = (Button) mRLayout.findViewById(R.id.backButton);
        playPause =  (Button) mRLayout.findViewById(R.id.playPause);
        skipButton = (Button) mRLayout.findViewById(R.id.skipSong);

        // Instantiate the RequestQueue.
         mImageLoader = CustomVolleyRequestQueue.getInstance(faActivity.getApplicationContext())
                .getImageLoader();

        tVcurrentTime = (TextView)mRLayout.findViewById(R.id.current_time);
        tVfinalTime = (TextView)mRLayout.findViewById(R.id.final_time);
        tVsongName = (TextView)mRLayout.findViewById(R.id.song_name);
        if (tVsongName !=null) tVsongName.setText(R.string.no_song_playing);

        seekbar = (SeekBar)mRLayout.findViewById(R.id.seekBar);
        seekbar.setClickable(false);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (">".equals(playPause.getText())) {
                    playPause.setText(R.string.pause);
                    playSong();
                }
                else {
                    playPause.setText(R.string.play);
                    ((PlaylistMakingActivity) faActivity).pauseSong();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = 0;
                ((PlaylistMakingActivity)faActivity).seekTo(startTime);
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = finalTime;
                ((PlaylistMakingActivity)faActivity).seekTo(startTime);
            }
        });


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                startTime = progressValue;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ((PlaylistMakingActivity)faActivity).pauseSong();
                currentlySeeking = true;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ((PlaylistMakingActivity)faActivity).seekTo(startTime);
                currentlySeeking = false;
            }
        });

        if(!Utils.isHost(faActivity.getIntent())){
            tVcurrentTime.setEnabled(false);
            tVsongName.setEnabled(false);
            tVfinalTime.setEnabled(false);
        }

        return mRLayout;
    }

    public void updateCurrentSong(SongData data){
        if (mCurrentUri == null || !mCurrentUri.equals(data.getURI())) {
            playSong();
        }
        mCurrentUri = data.getURI();
        String albumUrl = data.getAlbumArt();
        if (albumUrl != null) {
            NetworkImageView niv = (NetworkImageView) mRLayout.findViewById(R.id.imageView);
            if (albumUrl.length() > 0)
                niv.setImageUrl(albumUrl, mImageLoader);
            niv.setDefaultImageResId(R.drawable.pause);
            niv.setErrorImageResId(R.drawable.pause);
        }
        finalTime = ((PlaylistMakingActivity)faActivity).getDuration();
        tVfinalTime.setText(String.format(Locale.getDefault(), "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)))
        );
        tVsongName.setText(data.getSongName());
    }

    private final Runnable UpdateSongTime = new Runnable() {
        public void run() {

            double updatedTime = ((PlaylistMakingActivity)faActivity).getCurrentPosition();
            if (startTime > 0.0 && updatedTime == 0.0 && Utils.isHost(faActivity.getIntent())) {
                // song ended
                resetSongVoteTotal();
            }
            startTime = updatedTime;
            tVcurrentTime.setText(String.format(Locale.getDefault(), "%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );

            if (!currentlySeeking)
                seekbar.setProgress((int)startTime);
            myHandler.postDelayed(this, 100);
        }
    };


    private void resetSongVoteTotal(){
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    Utils.EventData eventData = new Utils.EventData(faActivity);

                    JSONObject songData = new JSONObject();

                    SongData data = ((PlaylistMakingActivity)faActivity).getCurrSongData();

                    songData.put("uri", data.getURI());
                    //reset vote count to 0 -- delta votes is -1 times current votes
                    songData.put("val", -1 * data.getVotes());

                    final String resp = Utils.attemptPOST("vote",
                            eventData.getEventName(), eventData.getPassword(),
                            new String[]{"song"},
                            new String[]{songData.toString()});

                    final String status = Utils.parseRespForStatus(resp);
                    if (!status.equals("OK"))
                        Utils.displayMsg(faActivity, status);
                }
                catch (Exception ex) {
                    Utils.displayMsg(faActivity, ex.toString());
                }

                //query database for newest song to play and play it
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

    private void playSong(){
        ((PlaylistMakingActivity)faActivity).seekTo(startTime);

        finalTime = ((PlaylistMakingActivity)faActivity).getDuration();
        startTime = ((PlaylistMakingActivity)faActivity).getCurrentPosition();

        if (oneTimeOnly == 0) {
            seekbar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }

        tVfinalTime.setText(String.format(Locale.getDefault(), "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)))
        );

        tVcurrentTime.setText(String.format(Locale.getDefault(), "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                startTime)))
        );

        seekbar.setProgress((int)startTime);
        if (oneTimeOnly == 0)
            myHandler.postDelayed(UpdateSongTime,100);
    }
}

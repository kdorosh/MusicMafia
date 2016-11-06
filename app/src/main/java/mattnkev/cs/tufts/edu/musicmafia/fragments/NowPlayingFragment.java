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
import mattnkev.cs.tufts.edu.musicmafia.activities.PlaylistMakingActivity;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

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

    private Handler myHandler = new Handler();
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    private SeekBar seekbar;
    private TextView tx1;
    public static int oneTimeOnly = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        faActivity = super.getActivity();

        setHasOptionsMenu(true);
        mRLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_now_playing, container, false);

        final Button b1,b2,b3,b4;
        b1 = (Button) mRLayout.findViewById(R.id.button);
        b2 = (Button) mRLayout.findViewById(R.id.button2);
        b3 = (Button) mRLayout.findViewById(R.id.button3);
        b4 = (Button) mRLayout.findViewById(R.id.button4);

        // Instantiate the RequestQueue.
         mImageLoader = CustomVolleyRequestQueue.getInstance(faActivity.getApplicationContext())
                .getImageLoader();

        final TextView tx2,tx3;
        tx1 = (TextView)mRLayout.findViewById(R.id.textView2);
        tx2 = (TextView)mRLayout.findViewById(R.id.textView3);
        tx3 = (TextView)mRLayout.findViewById(R.id.textView4);
        if (tx3!=null) tx3.setText(R.string.no_song_playing);

        seekbar = (SeekBar)mRLayout.findViewById(R.id.seekBar);
        seekbar.setClickable(false);
        b2.setEnabled(false);

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: remove Toast.makeText(faActivity.getApplicationContext(), "Playing sound",Toast.LENGTH_SHORT).show();
                ((PlaylistMakingActivity)faActivity).playSong();

                finalTime = startTime =((PlaylistMakingActivity)faActivity).getDuration();
                startTime =((PlaylistMakingActivity)faActivity).getCurrentPosition();

                if (oneTimeOnly == 0) {
                    seekbar.setMax((int) finalTime);
                    oneTimeOnly = 1;
                }

                tx2.setText(String.format(Locale.getDefault(), "%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                        finalTime)))
                );

                tx1.setText(String.format(Locale.getDefault(), "%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                        startTime)))
                );

                seekbar.setProgress((int)startTime);
                myHandler.postDelayed(UpdateSongTime,100);
                b2.setEnabled(true);
                b3.setEnabled(false);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: remove Toast.makeText(faActivity.getApplicationContext(), "Pausing sound",Toast.LENGTH_SHORT).show();
                ((PlaylistMakingActivity)faActivity).pauseSong();
                b2.setEnabled(false);
                b3.setEnabled(true);
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;

                if((temp+forwardTime)<=finalTime){
                    startTime = startTime + forwardTime;
                    ((PlaylistMakingActivity)faActivity).seekTo(startTime);
                    //TODO: remove Toast.makeText(faActivity.getApplicationContext(),"You have Jumped forward 5 seconds",Toast.LENGTH_SHORT).show();
                }else{
                    //TODO: remove Toast.makeText(faActivity.getApplicationContext(),"Cannot jump forward 5 seconds",Toast.LENGTH_SHORT).show();
                }
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int)startTime;

                if((temp-backwardTime)>0){
                    startTime = startTime - backwardTime;
                    ((PlaylistMakingActivity)faActivity).seekTo(startTime);
                    //TODO: remove Toast.makeText(faActivity.getApplicationContext(),"You have Jumped backward 5 seconds",Toast.LENGTH_SHORT).show();
                }else{
                    //TODO: remove Toast.makeText(faActivity.getApplicationContext(),"Cannot jump backward 5 seconds",Toast.LENGTH_SHORT).show();
                }
            }
        });

        return mRLayout;
    }

    public void updateAlbumArt(){
        String url = ((PlaylistMakingActivity)faActivity).getAlbumUrl();
        if (url != null) {
            NetworkImageView niv = (NetworkImageView) mRLayout.findViewById(R.id.imageView);
            if (url.length() > 0)
                niv.setImageUrl(url, mImageLoader);
            niv.setDefaultImageResId(R.drawable.pause);
            niv.setErrorImageResId(R.drawable.pause);
        }
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = ((PlaylistMakingActivity)faActivity).getCurrentPosition();
            tx1.setText(String.format(Locale.getDefault(), "%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            seekbar.setProgress((int)startTime);
            myHandler.postDelayed(this, 100);
        }
    };
}

package mattnkev.cs.tufts.edu.musicmafia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

/**
 * Created by Kevin on 10/21/2016.
 *
 * Collection of static utility methods to be shared by other classes
 */

public class Utils {
    public static final String SERVER_URL = "http://52.40.236.184:5000/";
    public static final String SPOTIFY_SERVER_URL = "https://api.spotify.com/v1/";
    public static final int PINGER_DELAY_SEC = 15;
    public static final int MIN_EVENT_NAME_LENGTH = 1;
    public static final int MIN_PASSWORD_LENGTH = 1;

    private static final String SERVER_DOWN_RESP = "{ \"Status\": \"The server is down\" }";

    /*
    * Attempts a POST request to our server. params and args must match up
    *
    * If not called as part of an AsyncTask may need to be called on background thread
    */
    public static String attemptPOST(String command, String eventName, String password,
                                     String[] params, String[] args) {
        HttpURLConnection conn = null;
        try {
            JSONObject postReqJSON = new JSONObject();
            if (params.length != args.length) { return "Invalid params/args"; }

            for (int c = 0; c < params.length; c++) {
                JSONObject json = isJSONValid(args[c]);
                if (json != null)
                    postReqJSON.put(params[c], json);
                else
                    postReqJSON.put(params[c], args[c]);
            }

            URL url = new URL(SERVER_URL + command);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            // encrypt event info and put in header
            byte[] encryption = encode("EventName: " + eventName + " password: " + password);
            String auth = Base64.encodeToString(encryption, Base64.NO_WRAP);
            conn.setRequestProperty("Authorization", auth);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(postReqJSON.toString());
            wr.flush();

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);

            return sb.toString();

        } catch (ConnectException ex) {
            return SERVER_DOWN_RESP;
        } catch (final Exception ex) {
            return ex.toString();
        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }

/*
 * Attempts a GET request to our server. params and args must match up
 *
 * If not called as part of an AsyncTask may need to be called on background thread
 */
    public static String attemptGET(String server, String command, String eventName, String password,
                                    String[] params, String[] args) {
        HttpURLConnection conn = null;
        try {

            String argsGET = "";
            if(params.length != args.length) { return "Invalid params/args"; }
            if(params.length>0)
                argsGET = "?" + params[0] + "=" + args[0];
            for(int c = 1; c < params.length; c++)
                argsGET += "&" + params[c] + "=" + args[c];

            String urlString = server + command + argsGET;
            URL url = new URL(urlString);
            conn = (HttpURLConnection)url.openConnection();

            if (server.equals(SERVER_URL)) {
                // encrypt event info and put in header
                byte[] encryption = encode("EventName: " + eventName + " password: " + password);
                String auth = Base64.encodeToString(encryption, Base64.NO_WRAP);
                conn.setRequestProperty("Authorization", auth);
            }

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);

            return sb.toString();

        } catch (ConnectException ex) {
            return SERVER_DOWN_RESP;
        } catch (final Exception ex){
            return ex.toString();
        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }

    public static boolean isHost(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String value = extras.getString("USER_TYPE");
            if (value != null && value.equals("Guest"))
                return false;
        }
        return true;
    }

    public static PlaylistData parseSpotifyResp(String resp) {
        try
        {
            JSONObject data = new JSONObject(resp);
            JSONObject tracks = data.getJSONObject("tracks");
            JSONArray entries = tracks.getJSONArray("items");

            final SongData[] songsData = new SongData[entries.length()];

            for (int i = 0; i < entries.length(); i++){
                JSONObject entry = entries.getJSONObject(i);
                String songName = entry.getString("name");
                String artistName = entry.getJSONObject("album").getJSONArray("artists").getJSONObject(0).getString("name");
                String uri = entry.getString("uri");
                String albumArt = entry.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
                int duration = Integer.parseInt(entry.getString("duration_ms"));

                songsData[i] = new SongData(songName, artistName, uri, albumArt, duration, 0);

            }
            return new PlaylistData(songsData);
        }
        catch (Exception ex)
        {
            Log.e("Utils: ParseSpotifyResp", ex.toString());
            return null;
        }
    }

    public static String parseRespForStatus(String resp) {
        try
        {
            JSONObject data = new JSONObject(resp);
            return data.getString("Status");
        }
        catch (Exception ex)
        {
            Log.e("parseRespForStatus", ex.toString());
        }
        return "JSON Object \"Status\" not found:" + resp;
    }

    public static PlaylistData parseCurrentPlaylist(String resp, FragmentActivity faActivity) {
        try {
            JSONObject data = new JSONObject(resp);
            JSONObject eventData = data.getJSONObject("Event");
            if (data.getString("Status").equals("OK")) {

                JSONArray songsJson = eventData.getJSONArray("songs");

                final SongData[] songsData = new SongData[songsJson.length()];

                for (int i = 0; i < songsJson.length(); i++) {
                    JSONObject songObj = songsJson.getJSONObject(i);

                    String songName = songObj.getString("name");
                    String artistName = songObj.getString("artist");
                    String uri = songObj.getString("uri");
                    String albumArt = songObj.getString("album_art");
                    int duration = Integer.parseInt(songObj.getString("ms_duration"));
                    int numVotes = Integer.parseInt(songObj.getString("val"));

                    songsData[i] = new SongData(songName, artistName, uri, albumArt, duration, numVotes);

                }

                return new PlaylistData(songsData);
            }
            else {
                displayMsg(faActivity, data.getString("Status"));
                return null;
            }
        }
        catch (Exception ex) {
            Log.e("parseAndUpdateCurrent..", ex.toString());
            displayMsg(faActivity, resp);
            return null;
        }
    }

    public static void displayMsg(final Activity activity, final String status) {

        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage(status);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // continue to activity anyways
                    }
                });
                alertDialog.show();
                }
            });
        }
    }

    public static class PlaylistData {
        private final SongData[] songs;

        private PlaylistData(SongData[] songDatas){
            songs = songDatas;
        }

        public SongData[] getSongDatas(){ return songs; }
    }

    public static class EventData {
        private final String eventName, password;

        public EventData(Activity faActivity){
            Bundle extras = faActivity.getIntent().getExtras();
            if (extras != null) {
                eventName = extras.getString("EVENT_NAME");
                password = extras.getString("PASSWORD");
            }
            else {
                eventName = "";
                password = "";
            }
        }

        public String getEventName() { return eventName; }
        public String getPassword() { return password; }
    }

    /* Private Utils methods */

    private static byte[] encode(String toBeEncoded) {
        // convert secret text to byte array
        final byte[] secret = toBeEncoded.getBytes();
        final byte[] encoded = new byte[secret.length];

        // Generate random key (has to be exchanged)
        final byte[] key = new byte[secret.length];
        new SecureRandom().nextBytes(key);

        // Encrypt
        for (int i = 0; i < secret.length; i++) {
            encoded[i] = (byte) (secret[i] ^ key[i]);
        }

        // Append key to encoded
        byte[] ret = new byte[encoded.length + key.length];
        System.arraycopy(encoded,0,ret,0,encoded.length);
        System.arraycopy(key,0,ret,encoded.length,key.length);

        return ret;
    }

    private static JSONObject isJSONValid(String test) {
        try {
            return new JSONObject(test);
        } catch (JSONException ex) {
            return null;
        }
    }

}

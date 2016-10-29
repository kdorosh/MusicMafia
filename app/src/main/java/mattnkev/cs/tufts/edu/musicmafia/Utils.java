package mattnkev.cs.tufts.edu.musicmafia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Kevin on 10/21/2016.
 *
 * Collection of static utility methods to be shared by other classes
 */

public class Utils {
    public static final String SERVER_URL = "http://52.40.236.184:5000/";
    public static final String SPOTIFY_SERVER_URL = "https://api.spotify.com/v1/";
    public static final int MAX_LISTVIEW_LEN = 20;
    public static final int PINGER_DELAY_SEC = 10;
    public static final int MIN_EVENT_NAME_LENGTH = 1;
    public static final int MIN_PASSWORD_LENGTH = 1;

    private static final String SERVER_DOWN_RESP = "{ \"Status\": \"The server is down\" }";

    /*
 * Attempts a POST request to our server. params and args must match up
 *
 * If not called as part of an AsyncTask may need to be called on background thread
 */
    public static String attemptPOST(String command, String[] params, String[] args) {
        HttpURLConnection conn = null;
        try {
            JSONObject postReqJSON = new JSONObject();
            if (params.length != args.length) { return "Invalid params/args"; }

            for(int c = 0; c < params.length; c++)
                postReqJSON.put(params[c], args[c]);

            URL url = new URL(SERVER_URL + command);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

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
    public static String attemptGET(String server, String command, String[] params, String[] args) {
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
            InputStream in = conn.getInputStream();
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

            final String[] searchListViewSongs = new String[MAX_LISTVIEW_LEN];
            final String[] searchListViewArtists = new String[MAX_LISTVIEW_LEN];
            final String[] searchListViewURIs = new String[MAX_LISTVIEW_LEN];
            int minLen = Math.min(Utils.MAX_LISTVIEW_LEN, entries.length());
            for (int i = 0; i < minLen; i++){
                JSONObject entry = entries.getJSONObject(i);
                searchListViewSongs[i] = entry.getString("name");
                searchListViewURIs[i] = entry.getString("uri");
                searchListViewArtists[i] = entry.getJSONObject("album").getJSONArray("artists").getJSONObject(0).getString("name");
            }
            return new PlaylistData(searchListViewSongs, searchListViewArtists, searchListViewURIs);
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
                String[] songs = new String[Utils.MAX_LISTVIEW_LEN],
                        artists = new String[Utils.MAX_LISTVIEW_LEN],
                        uris = new String[Utils.MAX_LISTVIEW_LEN];
                int minLen = Math.min(Utils.MAX_LISTVIEW_LEN, songsJson.length());
                for (int i = 0; i < minLen; i++) {
                    JSONObject songObj = songsJson.getJSONObject(i);
                    songs[i] = songObj.getString("name");
                    artists[i] = songObj.getString("artist");
                    uris[i] = songObj.getString("uri");
                }

                return new PlaylistData(songs, artists, uris);
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

    public static class PlaylistData {
        private final String[] searchListViewSongs;
        private final String[] searchListViewArtists;
        private final String[] searchListViewURIs;

        private PlaylistData(String[] songsList, String[] artists, String[] uris) {
            searchListViewSongs = songsList;
            searchListViewArtists = artists;
            searchListViewURIs = uris;
        }

        public String[] getSongs(){
            return searchListViewSongs;
        }
        public String[] getArtists() { return searchListViewArtists; }
        public String[] getURIs(){
            return searchListViewURIs;
        }
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


    public static void displayMsg(final Activity activity, final String status) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage(status);
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Continue", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface arg0, int arg1)
                {
                    // continue to activity anyways
                }
            });

            alertDialog.show();
            }
        });
    }

}

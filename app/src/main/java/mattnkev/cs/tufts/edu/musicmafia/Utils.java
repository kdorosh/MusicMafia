package mattnkev.cs.tufts.edu.musicmafia;

import android.content.Intent;
import android.os.Bundle;
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
 * Collection of utility methods to be shared by other classes
 */


public class Utils {
    public static final String SERVER_URL = "http://52.40.236.184:5000/";
    public static final String SPOTIFY_SERVER_URL = "https://api.spotify.com/v1/";
    public static final int MAX_LISTVIEW_LEN = 20;
    public static final int PINGER_DELAY_SEC = 10;
    public static final int MIN_EVENT_NAME_LENGTH = 4;
    public static final int MIN_PASSWORD_LENGTH = 4;


    /*
 * Attempts a POST request to our server. params and args must match up
 *
 * If not called as part of an AsyncTask may need to be called on background thread
 */
    public static String attemptPOST(String server, String command, String[] params, String[] args) {
        HttpURLConnection conn = null;
        try {
            JSONObject postReqJSON = new JSONObject();
            if(params.length != args.length) { return "Invalid params/args"; }

            for(int c = 0; c < params.length; c++)
                postReqJSON.put(params[c], args[c]);

            URL url = new URL(server + command);
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
            String response = sb.toString();

            JSONObject data = new JSONObject(response);
            return data.getString("Status");

        } catch (ConnectException ex) {
            //TODO: remove, this was for debugging (before the server was live)
            return "OK";
            //return "The server is down";
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
            //TODO: remove, this was for debugging (before the server was live)
            return "OK";
            //return "The server is down";
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
            if (value.equals("Guest"))
                return false;
        }
        return true;
    }

    public static SpotifyResp parseSpotifyResp(String resp) {
        try
        {
            JSONObject data = new JSONObject(resp);
            JSONObject tracks = data.getJSONObject("tracks");
            JSONArray entries = tracks.getJSONArray("items");

            final String[] searchListViewSongs = new String[MAX_LISTVIEW_LEN];
            final String[] searchListViewURIs = new String[MAX_LISTVIEW_LEN];
            for (int i = 0; i < MAX_LISTVIEW_LEN; i++){
                JSONObject entry = entries.getJSONObject(i);
                searchListViewSongs[i] = entry.getString("name");
                searchListViewURIs[i] = entry.getString("uri");
            }


            return new SpotifyResp(searchListViewSongs, searchListViewURIs);
            //runOnUiThread(new Runnable() {
            //    @Override
            //    public void run() {
            //        updateSearchFrag(searchListViewSongs, searchListViewURIs);
            //    }
            //});
        }
        catch (Exception ex)
        {
            Log.d("Utils", ex.toString());
            return null;
        }


    }

    public static class SpotifyResp {
        private String[] searchListViewSongs;
        private String[] searchListViewURIs;

        public SpotifyResp(String[] songsList, String[] uris) {
            searchListViewSongs = songsList;
            searchListViewURIs = uris;
        }

        public String[] getSearchListViewSongs(){
            return searchListViewSongs;
        }

        public String[] getSearchListViewURIs(){
            return searchListViewURIs;
        }

    }
}

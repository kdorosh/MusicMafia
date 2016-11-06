package mattnkev.cs.tufts.edu.musicmafia;

/**
 * Created by Kevin on 11/6/2016.
 *
 * Contains the contents we want stored on the client (typically in the PlaylistData object)
 * about a song
 */

public class SongData {
    private final String songName, artistName, uri, albumArtUrl;
    private final int songDuration;
    private int numberVotes;

    public SongData(String song, String artist, String URI,
                         String albumArtURL, int duration, int voteCount) {
        songName = song;
        artistName = artist;
        uri = URI;
        albumArtUrl = albumArtURL;
        songDuration = duration;
        numberVotes = voteCount;
    }

    public String getSongName(){
        return songName;
    }
    public String getArtist() { return artistName; }
    public String getURI(){
        return uri;
    }
    public String getAlbumArt() { return albumArtUrl; }
    public int getDuration() { return songDuration; }
    public int getVotes() { return numberVotes; }

    public void setVotes(int votes) {
        numberVotes = votes;
    }
}

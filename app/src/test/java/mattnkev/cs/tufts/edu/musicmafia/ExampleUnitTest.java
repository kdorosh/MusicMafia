package mattnkev.cs.tufts.edu.musicmafia;

import org.junit.Test;


/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */


public class ExampleUnitTest {
    @Test
    public void allTests() throws Exception {
        if(BuildConfig.DEBUG && !(querySpotifyServerTest()))
            throw new RuntimeException();
        if(BuildConfig.DEBUG && !(parseSpotifyRespTest()))
            throw new RuntimeException();
        if(BuildConfig.DEBUG && !(parseRespForStatusTest()))
            throw new RuntimeException();
    }


    private boolean querySpotifyServerTest() {
        String resp = Utils.attemptGET(Utils.SPOTIFY_SERVER_URL, "search",
                new String[]{"q", "type", "market"},
                new String[]{"mooo", "track", "US"});
        return expResp.equals(resp);
    }

    // this test caught an issue where less than 20 responses were given to be parsed
    private boolean parseSpotifyRespTest() {
        String[] EXPECTED_SONGS = {"Mooo", "Oogla Oogla Mooo"};
        String[] EXPECTED_ARTISTS = {"Gordon Halleck", "Toopy and Binoo"};
        String[] EXPECTED_URIS = {"spotify:track:7JopOEUM5krJ7LZEyuFuuy", "spotify:track:1fguq6qL9XfeZ9CD58hjzf"};

        Utils.PlaylistData spotifyResp = Utils.parseSpotifyResp(expResp);
        if (spotifyResp != null) {
            String[] songs = spotifyResp.getSongs();
            String[] artists = spotifyResp.getArtists();
            String[] uris = spotifyResp.getURIs();

            for (int i = 0; i < EXPECTED_SONGS.length; i++) {
                if (!songs[i].equals(EXPECTED_SONGS[i]) ||
                        !artists[i].equals(EXPECTED_ARTISTS[i]) ||
                        !uris[i].equals(EXPECTED_URIS[i])) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean parseRespForStatusTest() {
        return "my status".equals(Utils.parseRespForStatus("{\"Status\": \"my status\"}"));
    }


    private final String expResp = "{\n" +
            "  \"tracks\" : {\n" +
            "    \"href\" : \"https://api.spotify.com/v1/search?query=mooo&offset=0&limit=20&type=track&market=US\",\n" +
            "    \"items\" : [ {\n" +
            "      \"album\" : {\n" +
            "        \"album_type\" : \"album\",\n" +
            "        \"artists\" : [ {\n" +
            "          \"external_urls\" : {\n" +
            "            \"spotify\" : \"https://open.spotify.com/artist/6Ln0ACxqv6YOqKfiXZo00H\"\n" +
            "          },\n" +
            "          \"href\" : \"https://api.spotify.com/v1/artists/6Ln0ACxqv6YOqKfiXZo00H\",\n" +
            "          \"id\" : \"6Ln0ACxqv6YOqKfiXZo00H\",\n" +
            "          \"name\" : \"Gordon Halleck\",\n" +
            "          \"type\" : \"artist\",\n" +
            "          \"uri\" : \"spotify:artist:6Ln0ACxqv6YOqKfiXZo00H\"\n" +
            "        } ],\n" +
            "        \"available_markets\" : [ \"AD\", \"AR\", \"AT\", \"AU\", \"BE\", \"BG\", \"BO\", \"BR\", \"CA\", \"CH\", \"CL\", \"CO\", \"CR\", \"CY\", \"CZ\", \"DE\", \"DK\", \"DO\", \"EC\", \"EE\", \"ES\", \"FI\", \"FR\", \"GB\", \"GR\", \"GT\", \"HK\", \"HN\", \"HU\", \"ID\", \"IE\", \"IS\", \"IT\", \"JP\", \"LI\", \"LT\", \"LU\", \"LV\", \"MC\", \"MT\", \"MX\", \"MY\", \"NI\", \"NL\", \"NO\", \"NZ\", \"PA\", \"PE\", \"PH\", \"PL\", \"PT\", \"PY\", \"SE\", \"SG\", \"SK\", \"SV\", \"TR\", \"TW\", \"US\", \"UY\" ],\n" +
            "        \"external_urls\" : {\n" +
            "          \"spotify\" : \"https://open.spotify.com/album/7vU1YfJA26h2AZgFovCsER\"\n" +
            "        },\n" +
            "        \"href\" : \"https://api.spotify.com/v1/albums/7vU1YfJA26h2AZgFovCsER\",\n" +
            "        \"id\" : \"7vU1YfJA26h2AZgFovCsER\",\n" +
            "        \"images\" : [ {\n" +
            "          \"height\" : 640,\n" +
            "          \"url\" : \"https://i.scdn.co/image/dee31888e8aabcab21ed0597877170bef17256ee\",\n" +
            "          \"width\" : 640\n" +
            "        }, {\n" +
            "          \"height\" : 300,\n" +
            "          \"url\" : \"https://i.scdn.co/image/2f93f4b649bb89e291a607d953348718b900bba8\",\n" +
            "          \"width\" : 300\n" +
            "        }, {\n" +
            "          \"height\" : 64,\n" +
            "          \"url\" : \"https://i.scdn.co/image/447551dd005ed13e89169abb4fcf08af958eab0f\",\n" +
            "          \"width\" : 64\n" +
            "        } ],\n" +
            "        \"name\" : \"Bugger Pickers\",\n" +
            "        \"type\" : \"album\",\n" +
            "        \"uri\" : \"spotify:album:7vU1YfJA26h2AZgFovCsER\"\n" +
            "      },\n" +
            "      \"artists\" : [ {\n" +
            "        \"external_urls\" : {\n" +
            "          \"spotify\" : \"https://open.spotify.com/artist/6Ln0ACxqv6YOqKfiXZo00H\"\n" +
            "        },\n" +
            "        \"href\" : \"https://api.spotify.com/v1/artists/6Ln0ACxqv6YOqKfiXZo00H\",\n" +
            "        \"id\" : \"6Ln0ACxqv6YOqKfiXZo00H\",\n" +
            "        \"name\" : \"Gordon Halleck\",\n" +
            "        \"type\" : \"artist\",\n" +
            "        \"uri\" : \"spotify:artist:6Ln0ACxqv6YOqKfiXZo00H\"\n" +
            "      } ],\n" +
            "      \"available_markets\" : [ \"AD\", \"AR\", \"AT\", \"AU\", \"BE\", \"BG\", \"BO\", \"BR\", \"CA\", \"CH\", \"CL\", \"CO\", \"CR\", \"CY\", \"CZ\", \"DE\", \"DK\", \"DO\", \"EC\", \"EE\", \"ES\", \"FI\", \"FR\", \"GB\", \"GR\", \"GT\", \"HK\", \"HN\", \"HU\", \"ID\", \"IE\", \"IS\", \"IT\", \"JP\", \"LI\", \"LT\", \"LU\", \"LV\", \"MC\", \"MT\", \"MX\", \"MY\", \"NI\", \"NL\", \"NO\", \"NZ\", \"PA\", \"PE\", \"PH\", \"PL\", \"PT\", \"PY\", \"SE\", \"SG\", \"SK\", \"SV\", \"TR\", \"TW\", \"US\", \"UY\" ],\n" +
            "      \"disc_number\" : 1,\n" +
            "      \"duration_ms\" : 167746,\n" +
            "      \"explicit\" : false,\n" +
            "      \"external_ids\" : {\n" +
            "        \"isrc\" : \"uscgj0906997\"\n" +
            "      },\n" +
            "      \"external_urls\" : {\n" +
            "        \"spotify\" : \"https://open.spotify.com/track/7JopOEUM5krJ7LZEyuFuuy\"\n" +
            "      },\n" +
            "      \"href\" : \"https://api.spotify.com/v1/tracks/7JopOEUM5krJ7LZEyuFuuy\",\n" +
            "      \"id\" : \"7JopOEUM5krJ7LZEyuFuuy\",\n" +
            "      \"name\" : \"Mooo\",\n" +
            "      \"popularity\" : 0,\n" +
            "      \"preview_url\" : \"https://p.scdn.co/mp3-preview/a97cec583a86c48f0896a41824f25d9c180bd92d\",\n" +
            "      \"track_number\" : 13,\n" +
            "      \"type\" : \"track\",\n" +
            "      \"uri\" : \"spotify:track:7JopOEUM5krJ7LZEyuFuuy\"\n" +
            "    }, {\n" +
            "      \"album\" : {\n" +
            "        \"album_type\" : \"album\",\n" +
            "        \"artists\" : [ {\n" +
            "          \"external_urls\" : {\n" +
            "            \"spotify\" : \"https://open.spotify.com/artist/2Rnalq3hPC0gVGuUhnKfNf\"\n" +
            "          },\n" +
            "          \"href\" : \"https://api.spotify.com/v1/artists/2Rnalq3hPC0gVGuUhnKfNf\",\n" +
            "          \"id\" : \"2Rnalq3hPC0gVGuUhnKfNf\",\n" +
            "          \"name\" : \"Toopy and Binoo\",\n" +
            "          \"type\" : \"artist\",\n" +
            "          \"uri\" : \"spotify:artist:2Rnalq3hPC0gVGuUhnKfNf\"\n" +
            "        } ],\n" +
            "        \"available_markets\" : [ \"AD\", \"AR\", \"AT\", \"AU\", \"BE\", \"BG\", \"BO\", \"BR\", \"CH\", \"CL\", \"CO\", \"CR\", \"CY\", \"CZ\", \"DE\", \"DK\", \"DO\", \"EC\", \"EE\", \"ES\", \"FI\", \"FR\", \"GB\", \"GR\", \"GT\", \"HK\", \"HN\", \"HU\", \"ID\", \"IE\", \"IS\", \"IT\", \"JP\", \"LI\", \"LT\", \"LU\", \"LV\", \"MC\", \"MT\", \"MX\", \"MY\", \"NI\", \"NL\", \"NO\", \"NZ\", \"PA\", \"PE\", \"PH\", \"PL\", \"PT\", \"PY\", \"SE\", \"SG\", \"SK\", \"SV\", \"TR\", \"TW\", \"US\", \"UY\" ],\n" +
            "        \"external_urls\" : {\n" +
            "          \"spotify\" : \"https://open.spotify.com/album/29pLcPLRYK6SwVijDgF54D\"\n" +
            "        },\n" +
            "        \"href\" : \"https://api.spotify.com/v1/albums/29pLcPLRYK6SwVijDgF54D\",\n" +
            "        \"id\" : \"29pLcPLRYK6SwVijDgF54D\",\n" +
            "        \"images\" : [ {\n" +
            "          \"height\" : 640,\n" +
            "          \"url\" : \"https://i.scdn.co/image/ebcbc24023419fca7f8093c5709cb741628466b5\",\n" +
            "          \"width\" : 640\n" +
            "        }, {\n" +
            "          \"height\" : 300,\n" +
            "          \"url\" : \"https://i.scdn.co/image/6096c985bca001122616ab3c2e49e7bc10bb2824\",\n" +
            "          \"width\" : 300\n" +
            "        }, {\n" +
            "          \"height\" : 64,\n" +
            "          \"url\" : \"https://i.scdn.co/image/285371b70bff241e2a97cb4b3557fff03b4a5860\",\n" +
            "          \"width\" : 64\n" +
            "        } ],\n" +
            "        \"name\" : \"Toopy and Binoo and the Marshmallow Moon\",\n" +
            "        \"type\" : \"album\",\n" +
            "        \"uri\" : \"spotify:album:29pLcPLRYK6SwVijDgF54D\"\n" +
            "      },\n" +
            "      \"artists\" : [ {\n" +
            "        \"external_urls\" : {\n" +
            "          \"spotify\" : \"https://open.spotify.com/artist/2Rnalq3hPC0gVGuUhnKfNf\"\n" +
            "        },\n" +
            "        \"href\" : \"https://api.spotify.com/v1/artists/2Rnalq3hPC0gVGuUhnKfNf\",\n" +
            "        \"id\" : \"2Rnalq3hPC0gVGuUhnKfNf\",\n" +
            "        \"name\" : \"Toopy and Binoo\",\n" +
            "        \"type\" : \"artist\",\n" +
            "        \"uri\" : \"spotify:artist:2Rnalq3hPC0gVGuUhnKfNf\"\n" +
            "      } ],\n" +
            "      \"available_markets\" : [ \"AD\", \"AR\", \"AT\", \"AU\", \"BE\", \"BG\", \"BO\", \"BR\", \"CH\", \"CL\", \"CO\", \"CR\", \"CY\", \"CZ\", \"DE\", \"DK\", \"DO\", \"EC\", \"EE\", \"ES\", \"FI\", \"FR\", \"GB\", \"GR\", \"GT\", \"HK\", \"HN\", \"HU\", \"ID\", \"IE\", \"IS\", \"IT\", \"JP\", \"LI\", \"LT\", \"LU\", \"LV\", \"MC\", \"MT\", \"MX\", \"MY\", \"NI\", \"NL\", \"NO\", \"NZ\", \"PA\", \"PE\", \"PH\", \"PL\", \"PT\", \"PY\", \"SE\", \"SG\", \"SK\", \"SV\", \"TR\", \"TW\", \"US\", \"UY\" ],\n" +
            "      \"disc_number\" : 1,\n" +
            "      \"duration_ms\" : 103586,\n" +
            "      \"explicit\" : false,\n" +
            "      \"external_ids\" : {\n" +
            "        \"isrc\" : \"CA1L21000049\"\n" +
            "      },\n" +
            "      \"external_urls\" : {\n" +
            "        \"spotify\" : \"https://open.spotify.com/track/1fguq6qL9XfeZ9CD58hjzf\"\n" +
            "      },\n" +
            "      \"href\" : \"https://api.spotify.com/v1/tracks/1fguq6qL9XfeZ9CD58hjzf\",\n" +
            "      \"id\" : \"1fguq6qL9XfeZ9CD58hjzf\",\n" +
            "      \"name\" : \"Oogla Oogla Mooo\",\n" +
            "      \"popularity\" : 0,\n" +
            "      \"preview_url\" : \"https://p.scdn.co/mp3-preview/dd7d297a5934aa6ea1686979ab5ffe42aca4152b\",\n" +
            "      \"track_number\" : 5,\n" +
            "      \"type\" : \"track\",\n" +
            "      \"uri\" : \"spotify:track:1fguq6qL9XfeZ9CD58hjzf\"\n" +
            "    } ],\n" +
            "    \"limit\" : 20,\n" +
            "    \"next\" : null,\n" +
            "    \"offset\" : 0,\n" +
            "    \"previous\" : null,\n" +
            "    \"total\" : 2\n" +
            "  }\n" +
            "}";




}
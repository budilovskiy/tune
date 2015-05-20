/**
 * This class manages shuffle playing of the PlayList<Track>
 */
package com.gmail.budilovskiy.maksim;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class SoundJLayer {

    private final List<Track> CURRENT_PLAYLIST;
    private Track currentTrack;
    private URL url;
    private static HttpURLConnection connection;
    private static String stringTrackURL;
    private static InputStream is;
    private static BufferedInputStream bis;
    private static Thread audioThread;
    private static Player player;
    private static boolean isPlaying = true;

    private static final List<PlayListener> LISTENERS = new ArrayList<>();

    /**
     * constructor of SoundJLayer with new playlist
     *
     * @param playlist
     */
    public SoundJLayer(List<Track> playlist) {
        CURRENT_PLAYLIST = playlist;
    }

    /**
     * registering listeners
     *
     * @param listenerToAdd
     */
    public static void addListener(PlayListener listenerToAdd) {
        LISTENERS.add(listenerToAdd);
    }

    /**
     * @return track info (possibly may return any information: errors, etc.)
     */
    public String getTrackInfo() {
        return currentTrack.toString();
    }

    /**
     * stop playing current track and close input streams
     */
    public void stop() {
        if (player != null) {
            try {
                isPlaying = false;
                player.close();
                bis.close();
                is.close();
                connection.disconnect();
                player = null;
                for (PlayListener listener : LISTENERS) {
                    listener.PlayerStops();
                }
                /* for debugging */
                System.out.println(currentTrack.toString() + " was stopped.\n");
            } catch (IOException ex) {
                Logger.getLogger(SoundJLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * plays random track of playlist
     *
     * @param playlist to play
     */
    public void play(List<Track> playlist) {
        stop();
        //currentPlaylist = playlist;
        currentTrack = playlist.get(new Random().nextInt(playlist.size()));
        currentTrack.setTrackURL(currentTrack.toString());
        stringTrackURL = currentTrack.getTrackURL();
        playTrack(stringTrackURL);
    }

    /**
     * plays single track of playlist
     *
     * @param trackURL
     */
    private void playTrack(final String trackURL) {
        if (trackURL != null) {
            try {
                url = new URL(trackURL);
                connection = (HttpURLConnection) url.openConnection();
                is = connection.getInputStream();
                bis = new BufferedInputStream(is);
            } catch (MalformedURLException | ConnectException ex) {
                Logger.getLogger(SoundJLayer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SoundJLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } /* for debugging */ else {
            System.out.println("\n");
        }

        audioThread = new Thread("audioThread") {
            @Override
            public void run() {
                try {
                    isPlaying = true;
                    if (trackURL != null) {
                        for (PlayListener listener : LISTENERS) {
                            listener.PlayerStarts();
                        }
                        player = new Player(bis);
                        /* for debugging */
                        System.out.println(currentTrack.toString() + " is playing...");
                        player.play();
                    }
                } catch (JavaLayerException ex) {
                    // skip track if JavaLayerException
                    Logger.getLogger(SoundJLayer.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (isPlaying) {
                    play(CURRENT_PLAYLIST);
                }
            }
        };
        audioThread.start();
        isPlaying = false;
    }
}

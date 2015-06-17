/**
 * This class manage shuffle playing of the PlayList<Track>
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
    private static boolean playing = true;
    private static boolean random = true;
    private int trackIndex = 0;

    public void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

    public static boolean isPlaying() {
        return playing;
    }

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
                System.out.println("\nplayer is not null. Closing...");
                playing = false;
                player.close();
                bis.close();
                is.close();
                connection.disconnect();
                player = null;
                for (PlayListener listener : LISTENERS) {
                    listener.PlayerStops();
                }
                /* for debugging */
                System.out.println(currentTrack.toString() + " was stopped.");
            } catch (IOException ex) {
                Logger.getLogger(SoundJLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("\nplayer is null");
        }
    }

    /**
     * plays random track of playlist
     *
     * @param playlist to play
     * @param random
     */
    public void play(List<Track> playlist, boolean random) {
        stop();
        if (random) {
            currentTrack = playlist.get(new Random().nextInt(playlist.size()));
        } else {
            currentTrack = playlist.get(trackIndex);
        }
        stringTrackURL = currentTrack.getTrackURL();
        if (stringTrackURL == null) {
            currentTrack.setTrackURL(currentTrack.toString());
            stringTrackURL = currentTrack.getTrackURL();
        }
        if (trackIndex < playlist.size() - 1) {
            trackIndex += 1;
        }
        playTrack(stringTrackURL);
    }

    /**
     * plays single track from playlist
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
                    playing = true;
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
                if (playing) {
                    play(CURRENT_PLAYLIST, random);
                }
            }
        };
        audioThread.start();
        playing = false;
    }
}

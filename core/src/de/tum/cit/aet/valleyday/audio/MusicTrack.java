package de.tum.cit.aet.valleyday.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * This enum is used to manage the music tracks in the game.
 * Currently, only one track is used, but this could be extended to include multiple tracks.
 * Using an enum for this purpose is a good practice, as it allows for easy management of the music tracks
 * and prevents the same track from being loaded into memory multiple times.
 * See the assets/audio folder for the actual music files.
 * Feel free to add your own music tracks and use them in the game!
 */
public enum MusicTrack {

    /** Music played in the main menu.  */
    MENU("menu.mp3", 0.2f),
    /** Music played during active gameplay.  */
    BACKGROUND("mushroom dance.mp3", 0.4f);
    

    /** The music file owned by this variant. */
    private final Music music;
    
    MusicTrack(String fileName, float volume) {
        this.music = Gdx.audio.newMusic(Gdx.files.internal("audio/" + fileName));
        this.music.setLooping(true);
        this.music.setVolume(volume);
    }
    
    /**
     * Plays this track and stops all other MusicTracks.
     */

    public void play() {
        for (MusicTrack track : MusicTrack.values()) {
            track.music.stop();
        }
        this.music.play();
    }

    /**
     * Static method to stop all currently defined music tracks.
     */
    public static void stopAll() {
        for (MusicTrack track : MusicTrack.values()) {
            if (track.music != null) {
                track.music.stop();
            }
        }
    }
}

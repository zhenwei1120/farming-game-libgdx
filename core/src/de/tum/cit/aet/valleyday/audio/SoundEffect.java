package de.tum.cit.aet.valleyday.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

/**
 * Enumeration of sound effects used in the game.
 */

public enum SoundEffect {

    /** Played when picking up a tool.  */
    PICKUP("tool.wav"),
    
    /** Played during crop harvesting. 。 */
    HARVEST("harvest.wav"),
    
    /** Played when the player reaches a victory condition.  */
    VICTORY("win.wav"),
    
    /** Played when the player loses the game.  */
    LOSE("failed.mp3"),
    
    /** Interaction sound for chickens. */
    CHICKEN("chicken.mp3"),
    
    /** Interaction sound for birds. */
    BIRD("bird.ogg"),
    
    /** Played when a scare event is triggered. */
    SCARE("pained.ogg"),
    
    /** Played when clearing debris from the map.  */
    CLEAR_DEBRIS("debris.ogg");

    /**
     * Internal constructor that handles asset loading and error reporting.
     * * @param fileName The filename within the "assets/audio/" directory. 
     */
    private final Sound sound;

     SoundEffect(String fileName) {
        Sound tmp = null;
        try {
            var file = Gdx.files.internal("audio/" + fileName);
            System.out.println(
                "[SoundEffect] Loading: audio/" + fileName +
                " | exists=" + file.exists()
            );
            tmp = Gdx.audio.newSound(file);
            System.out.println(
                "[SoundEffect] SUCCESS: audio/" + fileName
            );
        } catch (Exception e) {
            System.err.println(
                "[SoundEffect] FAILED: audio/" + fileName
            );
            e.printStackTrace();
        }
        this.sound = tmp;
    }

    /**
     * Plays the sound effect at a preset volume.
     * * This method includes a null-check to prevent crashes if the sound file failed to load.
     */
    public void play() {
    if (this.sound != null) {
        this.sound.play(0.4f); 
    } else {
        System.err.println("[SoundEffect] Skipping play() because sound is null. Check your file path!");
        }
    }

    /**
     * Releases all sound resources from memory.
     * * Should be called during the game's shutdown or screen disposal to prevent memory leaks.
     */
    public static void disposeAll() {
    for (SoundEffect s : SoundEffect.values()) {
        if (s.sound != null) {
            s.sound.dispose();
            }
        }
    }

}
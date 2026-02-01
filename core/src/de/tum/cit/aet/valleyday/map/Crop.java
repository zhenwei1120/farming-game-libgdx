package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.Textures;

/**
 * Represents a plant in the game that grows over time and eventually rots.
 * * <p>Growth follows a timeline: Seed -> Grow1 -> Grow2 -> Mature -> Rotten.</p>
 */
public class Crop implements Drawable{

    /** * Enumeration of the growth stages. */
    public enum Stage {
        SEED, GROW1, GROW2, MATURE, ROTTEN
    }

    private final float x;
    private final float y;

    /** Current age of the crop in seconds. */
    private float age = 0f;
    /** Age at which the crop becomes rotten. */
    private float rottenAtAge = MATURE_TIME;//

    private Stage stage = Stage.SEED;

    private static final float SEED_TIME = 2f;
    private static final float GROW1_TIME = 4f;
    private static final float GROW2_TIME = 6f;
    private static final float MATURE_TIME = 20f; 

    /**
     * Constructs a crop at a specific grid position.
     * @param x Tile X coordinate. 
     * @param y Tile Y coordinate. 
     */
    public Crop(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Updates the growth stage based on elapsed time.
     * @param time Seconds passed since the last frame. 
     */
    public void tick(float time) {
        age += time;

        if (age < SEED_TIME) stage = Stage.SEED;
        else if (age < GROW1_TIME) stage = Stage.GROW1;
        else if (age < GROW2_TIME) stage = Stage.GROW2;
        else if (age < rottenAtAge) stage = Stage.MATURE;
        else stage = Stage.ROTTEN;
    }

    public boolean isMature() { 
        return stage == Stage.MATURE; 
    }
    public boolean isRotten() { 
        return stage == Stage.ROTTEN;
    }
    public Stage getStage() { 
        return stage; 
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        switch (stage) {
            case SEED:    return Animations.CROP_SEED;
            case GROW1: return Animations.CROP_GROW1;
            case GROW2: return Animations.CROP_GROW2;
            case MATURE:  return Animations.CROP_MATURE;
            case ROTTEN:  return Animations.CROP_ROTTEN;
        }
        return Animations.CROP_SEED;
    }
    
    public void fertilizeAdvanceOneStage() {
        if (stage == Stage.MATURE || stage == Stage.ROTTEN) return;
        switch (stage) {
            case SEED: age = SEED_TIME;
                break;
            case GROW1: age = GROW1_TIME;
                break;
            case GROW2: age = GROW2_TIME;
                break;
            default: break;
        }
            tick(0f);
    }

    public void resetRottenTimer(float secondsFromNow) {
        rottenAtAge = age + secondsFromNow;
        tick(0f);
    }

    public void reviveRottenToMatureAndExtend(float secondsFromNow) {
        if (stage == Stage.ROTTEN) {
            age = GROW2_TIME;
        }
        rottenAtAge = age + secondsFromNow;
        tick(0f);
    }

    @Override
    public float getX() {
        return x;
    }
    
    @Override
    public float getY() {
        return y;
    }
   
    public int getTileX() {
    return (int) x;
    }

    public int getTileY() {
    return (int) y;
    }

}
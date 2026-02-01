package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;


/**
 * Represents a fertilizer item on the map that can be picked up by the player.
 * * <p>When picked up, it immediately advances the growth stage of all crops on the map.</p>
 */
public class Fertilizer implements Drawable {
    private final float x;
    private final float y;

    /** Indicates if the item has already been collected. */
    private boolean pickedUp = false;

    /**
     * Constructs a Fertilizer instance at the given coordinates.
     * @param x Tile X coordinate. 
     * @param y Tile Y coordinate. 
     */
    public Fertilizer(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /** @return True if the fertilizer has been picked up.  */
    public boolean isPickedUp() { return pickedUp; }

    /**
     * Handles the collection logic: updates inventory and triggers a global crop growth boost.
     * * @param player The player instance picking up the item. 
     * @param map    The map instance where crops will be fertilized. 
     */
    public void pickup(Player player, GameMap map) {
        if (pickedUp) return;
        pickedUp = true;
        player.getInventory().pickUp("fertilizer");
        map.fertilizeAllCropsInstantly();
        System.out.println("Picked up fertilizer! All crops advanced one stage.");
    }

    /** @return The X coordinate in world units. */
    @Override public float getX() { return x; }
    
    /** @return The Y coordinate in world units. */
    @Override public float getY() { return y; }

    /** @return The texture region for the fertilizer. */
    @Override
    public TextureRegion getCurrentAppearance() {
        return Animations.FERTILIZER;
    }
}


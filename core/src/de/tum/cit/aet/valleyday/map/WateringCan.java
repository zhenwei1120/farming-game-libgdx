package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;

/**
 * Represents a Watering Can item on the map.
 * The watering can provides a one-time global rescue effect for crops upon pickup.
 */
public class WateringCan implements Drawable {
    private final float x, y;
    private boolean pickedUp = false;

    /**
     * Constructs a Watering Can at the specified grid coordinates.
     * @param x The X-coordinate on the map.
     * @param y The Y-coordinate on the map.
     */
    public WateringCan(float x, float y) {
        this.x = x; this.y = y;
    }

    /** @return true if the watering can has been collected by the player. */
    public boolean isPickedUp() { return pickedUp; }

    /**
     * Handles the collection of the watering can.
     * <p>Adds the item to the player's inventory and triggers a map-wide effect 
     * that revives rotten crops and increases their lifespan.</p>
     * @param player The player collecting the item.
     * @param map The game map to apply global effects to.
     */
    public void pickup(Player player, GameMap map) {
        if (pickedUp) return;
        pickedUp = true;

        player.getInventory().pickUp("wateringCan"); 
        map.applyWateringCanGlobalEffect(); 

        System.out.println("Picked up watering can! Rotten crops revived & rot timer extended.");
    }

    @Override public float getX() { return x; }
    @Override public float getY() { return y; }

    @Override
    public TextureRegion getCurrentAppearance() {
        return Animations.WATERCAN;
    }
}


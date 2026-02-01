package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;

/**
 * Represents a Shovel item on the map that can be picked up by the player.
 * The shovel is a permanent upgrade that increases the efficiency of clearing debris.
 */

public class Shovel implements Drawable {
    private final float x;
    private final float y;
    private boolean pickedUp = false;

    /**
     * Constructs a new Shovel at the specified coordinates.
     * @param x The X-coordinate on the grid.
     * @param y The Y-coordinate on the grid.
     */
    public Shovel(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /** @return true if the shovel has already been collected. */
    public boolean isPickedUp() {
        return pickedUp;
    }

    /**
     * Handles the collection of the shovel when the player interacts with it.
     * <p>Updates the player's inventory and applies a permanent upgrade to 
     * the player's debris-clearing speed.</p>
     * @param player The player instance collecting the item.
     */
    public void pickup(Player player) {
        if (pickedUp) return;
        pickedUp = true;

        player.getInventory().pickUp("shovel");

        player.applyShovelUpgrade();

        System.out.println("Picked up shovel!");
    }

    // Drawable
    @Override public float getX() { return x; }
    @Override public float getY() { return y; }

    @Override
    public TextureRegion getCurrentAppearance() {
        return Animations.SHOVEL;
    }
}
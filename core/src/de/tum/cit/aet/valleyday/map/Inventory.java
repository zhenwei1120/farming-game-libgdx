package de.tum.cit.aet.valleyday.map;

/**
 * Manages the player's collected items and tool availability.
 * <p>This class tracks which special items (Shovel, Watering Can, Fertilizer) 
 * the player currently possesses in their backpack.</p>
 */

public class Inventory {

    /** Player's tools. */
    private boolean hasShovel = false;
    private boolean hasWateringCan = false;
    private boolean hasFertilizer = false;

    public boolean getHasShovel(){
        return hasShovel;
    }
    public boolean getHasWateringCan(){
        return hasWateringCan;
    }
    public boolean getHasFertilizer(){
        return hasFertilizer;
    }

    /**
     * Calculates the remaining duration for timed power-ups.
     * <p>Currently returns 0 as items in this version are permanent once collected.</p>
     * @param elapsedTime Time passed in the current game session. 
     * @return Remaining time in seconds. 
     */
    public float getRemainingTime(float elapsedTime) {
        return 0f;
    }

    /**
     * Updates tool status when an item is collected.
     * @param item The name of the item picked up.
     */
    //pick up
    public void pickUp(String item){
        if ("shovel".equals(item)) {
            hasShovel = true;
        }else if ("wateringCan".equals(item)){
            hasWateringCan = true;
        }else if ("fertilizer".equals(item)) {
            hasFertilizer = true;
        }
    }
}
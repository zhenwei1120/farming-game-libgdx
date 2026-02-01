package de.tum.cit.aet.valleyday.core;

/**
 * GameState manages the overall progress, including harvest goals, 
 * time tracking, and game completion status.
 */
public class GameState {
    private int harvested = 0;
    private final int goal = 5;
    private boolean exitUnlocked = false;
    private final float totalTimeLimit = 180.0f;

    /**
     * Increments the harvest count and checks if the win condition is met.
     */
    public void addHarvest() {
        harvested++;

        if (harvested >= goal && !exitUnlocked) {
            exitUnlocked = true;
        }
    }

    /**
     * Calculates the remaining game time.
     * @param elapsedTime Time since the start of the level.
     * @return Remaining seconds as an integer. 
     */
    public int getRemainingTime(float elapsedTime) {
        return Math.max(0, (int)(totalTimeLimit - elapsedTime));
    }

    public int getHarvested() { return harvested; }
    public int getGoal() { return goal; }
    public boolean isExitUnlocked() { return exitUnlocked; }
}
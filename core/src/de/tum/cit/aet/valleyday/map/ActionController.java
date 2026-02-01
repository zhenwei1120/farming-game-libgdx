package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import de.tum.cit.aet.valleyday.audio.SoundEffect;
import de.tum.cit.aet.valleyday.core.GameState;

/**
 * ActionController: Handles player interactions.
 */
public class ActionController {

    private final Player player;
    private final GameMap gameMap;
    private final GameState gameState;

    // Timer to track how many seconds a key is held
    private float holdTimer = 0f;

    /**
     * Constructor for the controller.
     * @param player The player performing actions. 
     * @param gameMap The map where actions happen.
     * @param gameState The current state of the game for recording stats. 
     */ 
    public ActionController(Player player, GameMap gameMap,GameState gameState) {
        this.player = player;
        this.gameMap = gameMap;
        this.gameState = gameState;
    }

    /**
     * Checks for player actions every time the game updates.
     * @param secondsPassed Time elapsed since the last update.
     */
    public void checkPlayerActions(float secondsPassed) {
        // A Key: Quick Action (Plant/Harvest) 

        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            doQuickAction();
        }

        // D Key: Long Action (Clear Debris) 
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            holdTimer += secondsPassed;

            float requiredTime = getClearRequiredTime();

            if (holdTimer >= requiredTime) {
                doLongAction();
                holdTimer = 0f; // Reset timer after trigger
            }
        } else {
            holdTimer = 0f; // Reset timer if key is released 
        }

        //S Key:doscareaction
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            doScareAction();
        }
    }
    
    /**
     * Handles the logic for a quick press of the A key.
     * Plant seeds in the empty squares in front of you, or harvest the crops already there.
     */
    private void doQuickAction() {
    int targetX = getTargetX();
    int targetY = getTargetY();
    int type = gameMap.getCellType(targetX, targetY);

    // Situation A: The area directly ahead is open ground (3), suitable for sowing.
    if (type == 3) {
        Crop newCrop = new Crop(targetX, targetY);
        gameMap.getCrops().add(newCrop);
        // Set this tile to 5 (crops), and players won't be able to pass through.
        gameMap.setCellType(targetX, targetY, 5); 
        System.out.println("Planted at " + targetX + "," + targetY);
    } 
    // B: Crops are already present directly ahead (5), attempt to harvest.
    else if (type == 5) {
        Crop foundItem = findItemAt(targetX, targetY);
        if (foundItem != null && foundItem.isMature()) {
            gameMap.getCrops().remove(foundItem);
            gameMap.setCellType(targetX, targetY, 3); 
            gameState.addHarvest();
            SoundEffect.HARVEST.play();
            System.out.println("Harvested!");
        } else {
            System.out.println("Wait for it to grow...");
        }
    }
}

    /**
     * Handles the logic for holding the D key for 1 second.
     */
    private void doLongAction() {
        int targetX = getTargetX();
        int targetY = getTargetY();

        int t = gameMap.getCellType(targetX, targetY);

        // 0 = Wall: Take no action
        if (t == 0) return;

        // 1 = Shredded branches: Clear away shredded branches (convert to green space)
        if (t == 1) {
            gameMap.clearDebris(targetX, targetY);
            SoundEffect.CLEAR_DEBRIS.play();
            System.out.println("Cleared debris at: " + targetX + "," + targetY);
            return;
        }

        // Other situations: Consider cleaning up rotten crops.
        Crop foundItem = findItemAt(targetX, targetY);
        if (foundItem != null && foundItem.isRotten()) {
            gameMap.getCrops().remove(foundItem);
            SoundEffect.CLEAR_DEBRIS.play();
            System.out.println("Cleared rotten crop at: (" + targetX + ", " + targetY + ")");
        }
    }
        /**
          * Scares away wild animals within a specific radius of the player.
          */
        private void doScareAction() {
            int px = (int) player.getX();
            int py = (int) player.getY();

            float radius = 1.5f;
            float r2 = radius * radius; // 2.25

            boolean scaredSomeone = false;

            // Animal 1: Can expel all within its radius.
            for (WildAnimal1 a : gameMap.getAnimals1()) {
                if (a == null || !a.isAlive()) continue;

                int dx = a.getTileX() - px;
                int dy = a.getTileY() - py;
                if (dx * dx + dy * dy <= r2) {
                    SoundEffect.CHICKEN.play();
                    a.scareAwayFrom(px, py);
                    scaredSomeone = true;
                }
            }

            // Animal 2: Can expel all within its radius.
            for (WildAnimal2 b : gameMap.getAnimals2()) {
                if (b == null || !b.isAlive()) continue;

                int dx = b.getTileX() - px;
                int dy = b.getTileY() - py;
                if (dx * dx + dy * dy <= r2) {
                    SoundEffect.BIRD.play();
                    b.scareAwayFrom(px, py);
                    scaredSomeone = true;
                }
            }

            if (!scaredSomeone) {
                System.out.println("No animal close enough.");
            }
        }

    /**
     * Helper to find an item at specific coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @return The found crop or null
     */
    private Crop findItemAt(int x, int y) {
        for (Crop c : gameMap.getCrops()) {
            if ((int) c.getX() == x && (int) c.getY() == y) {
                return c;
            }
        }
        return null;
    }

    /**
     * Calculates the X coordinate directly in front of the player.
     */
    private int getTargetX() {
        int x = (int) player.getX();
        String dir = player.getFacingDirection(); // Call the direction method in Player | 调用 Player 里的方向方法

        if ("LEFT".equals(dir)) return x - 1;
        if ("RIGHT".equals(dir)) return x + 1;
        return x;
    }

    /**
     * Calculates the Y coordinate directly in front of the player.
     */
    private int getTargetY() {
        int y = (int) player.getY();
        String dir = player.getFacingDirection(); // Call the direction method in Player | 调用 Player 里的方向方法

        if ("UP".equals(dir)) return y + 1;
        if ("DOWN".equals(dir)) return y - 1;
        return y;
    }

    /**
     * Gets the required hold time for clearing actions from the player's attributes.
     * @return The time in seconds required to clear debris.
     */
    private float getClearRequiredTime() {
        return player.getBranchClearSeconds();
    }
}
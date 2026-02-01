package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import de.tum.cit.aet.valleyday.core.GameState;
import de.tum.cit.aet.valleyday.map.Inventory;

/**
 * A Heads-Up Display (HUD) responsible for overlaying game statistics and UI information.
 * * <p>It utilizes a dedicated {@link OrthographicCamera} to maintain a fixed position on the screen,
 * ensuring UI elements do not scroll with the game world camera.</p>
 */
public class Hud {
    
    /** The SpriteBatch used to draw the HUD. This is the same as the one used in the GameScreen. */
    private final SpriteBatch spriteBatch;
    /** The font used to draw text on the screen. */
    private final BitmapFont font;
    /** The camera used to render the HUD. */
    private final OrthographicCamera camera;

    private final GameState gameState;
    private final Inventory inventory;

    /**
     * Constructs the HUD and initializes its static camera.
     * * @param spriteBatch The batch shared with the game screen. 
     * @param font        The font used for HUD text. 
     * @param gameState   The source for game progression data (time, harvest). 
     * @param inventory   The source for player tool information.
     */
    public Hud(SpriteBatch spriteBatch, BitmapFont font,GameState gameState, Inventory inventory) {
        this.spriteBatch = spriteBatch;
        this.font = font;
        this.camera = new OrthographicCamera();
        this.gameState = gameState;
        this.inventory = inventory;
    }
    
    /**
     * Renders the HUD elements using its own projection matrix.
     * * <p>This method switches the SpriteBatch to use the HUD's static camera, 
     * draws information like remaining time and inventory, and then ends the batch.</p>
     * * @param totalTime Total elapsed game time, used to calculate daylight logic. 
     */
   public void render(float totalTime) {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        float screenH = Gdx.graphics.getHeight();
        float x = 20; 

        // Remaining Daylight
        int timeLeft = gameState.getRemainingTime(totalTime);
        font.draw(spriteBatch, "Daylight Left: " + timeLeft + "s", x, screenH - 20);

        // Harvest Progress 
        font.draw(spriteBatch, "Crops: " + gameState.getHarvested() + " / " + gameState.getGoal(), x, screenH - 50);

        // Collected Tools
        String tools = "Tools: " + (inventory.getHasShovel() ? "[Shovel] " : "") 
                                 + (inventory.getHasWateringCan() ? "[Can] " : "")
                                 + (inventory.getHasFertilizer() ? "[Fert] " : "");
        font.draw(spriteBatch, tools, x, screenH - 80);

        // Exit Unlock Status
        if (gameState.isExitUnlocked()) {
            font.setColor(com.badlogic.gdx.graphics.Color.YELLOW); 
            font.draw(spriteBatch, "!!! EXIT OPEN: FIND THE GATE !!!", x, screenH - 110);
            font.setColor(com.badlogic.gdx.graphics.Color.WHITE); 
        } else {
            font.draw(spriteBatch, "Exit: LOCKED", x, screenH - 110);
        }

        // Control Hints
        font.draw(spriteBatch, "Press Esc to Pause", x, screenH - 140);

        spriteBatch.end();
    }
    
    /**
     * Resizes the HUD when the screen size changes.
     * This is called when the window is resized.
     * @param width The new width of the screen.
     * @param height The new height of the screen.
     */
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }
}

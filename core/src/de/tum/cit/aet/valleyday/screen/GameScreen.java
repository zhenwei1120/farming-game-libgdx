package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.audio.MusicTrack;
import de.tum.cit.aet.valleyday.audio.SoundEffect;
import de.tum.cit.aet.valleyday.core.CameraController;
import de.tum.cit.aet.valleyday.core.GameState;
import de.tum.cit.aet.valleyday.map.ActionController;
import de.tum.cit.aet.valleyday.map.Crop;
import de.tum.cit.aet.valleyday.map.Fertilizer;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.Player;
import de.tum.cit.aet.valleyday.map.Shovel;
import de.tum.cit.aet.valleyday.map.WateringCan;
import de.tum.cit.aet.valleyday.map.WildAnimal1;
import de.tum.cit.aet.valleyday.map.WildAnimal2;
import de.tum.cit.aet.valleyday.texture.Animations;

/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 * *  <p>It coordinates between the {@link GameMap}, {@link Player}, and {@link Hud} 
 * to create the interactive farm experience.</p>
 */
public class GameScreen implements Screen {
    /**
     * The size of a grid cell in pixels.
     * This allows us to think of coordinates in terms of square grid tiles
     * (e.g. x=1, y=1 is the bottom left corner of the map)
     * rather than absolute pixel coordinates.
     */
    public static final int TILE_SIZE_PX = 16;
    
    /**
     * The scale of the game.
     * This is used to make everything in the game look bigger or smaller.
     */
    public static final int SCALE = 4;

    private final ValleyDayGame game;
    private final SpriteBatch spriteBatch;
    private final GameMap map;
    private final Hud hud;
    private final OrthographicCamera mapCamera;
    private final Viewport viewport;
    private final Player player;
    private final GameState gameState;

    /** Accumulated time since the start of the game session. */
    private float totalTime = 0;

    private final ActionController actionController;
    private final CameraController cameraController;

    /** Flag to prevent updates when the game is paused.  */
    private boolean isPaused = false; 
    
    /** Flag indicating if the game has reached a win or loss state.  */
    private boolean ended = false;       

    /**
     * Constructor for GameScreen. Sets up the camera and font.
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(ValleyDayGame game) {
        this.game = game;
        this.spriteBatch = game.getSpriteBatch();
        this.map = game.getMap();
        this.gameState = new GameState();
        this.player = map.getPlayer();
        this.hud = new Hud(spriteBatch, game.getSkin().getFont("font"),gameState,player.getInventory());
        // Create and configure the camera for the game view
        this.mapCamera = new OrthographicCamera();
        this.mapCamera.setToOrtho(false);
        //Prevent scaling 
        this.viewport = new ExtendViewport(800, 600, mapCamera);
        this.actionController = new ActionController(player, map, gameState);
        this.cameraController = new CameraController(mapCamera);
    }

    /**
     * The render method is called every frame to render the game.
     * @param deltaTime The time in seconds since the last render.
     */
    @Override
    public void render(float deltaTime) {
        if(ended)return;

        // Handle Escape menu transition
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game, this)); 
            return;
        }
        
        // Clear the previous frame from the screen, or else the picture smears
        ScreenUtils.clear(Color.BLACK);

        // Failure Condition Check
        if (gameState.getRemainingTime(totalTime) <= 0 || map.isGameOver()) {
            ended = true;
            MusicTrack.stopAll();
            SoundEffect.LOSE.play();
            game.setScreen(new EndScreen(game, false));
            return; 
        }

        // Victory Condition Check 
        int px = (int)player.getX();
        int py = (int)player.getY();
        if (map.getCellType(px, py) == 4 && map.isExitUnlocked()) {
            ended = true;
            MusicTrack.stopAll(); 
            SoundEffect.VICTORY.play();
            game.setScreen(new EndScreen(game, true));
            return; 
        }
        
        // Cap frame time to 250ms to prevent spiral of death
        float frameTime = Math.min(deltaTime, 0.250f);
        if (!isPaused && !map.isGameOver()) {
        // Update the map state
        map.tick(frameTime);

        map.spawnShovelIfNeeded();
        checkShovelPickup();
        map.spawnFertilizerIfNeeded();
        checkFertilizerPickup();
        map.spawnWateringCanIfNeeded();
        checkWateringCanPickup();
        map.spawnAnimalIfNeeded();

        actionController.checkPlayerActions(frameTime);

        totalTime += deltaTime;
        }
        
        // Update the camera
        int size = TILE_SIZE_PX * SCALE;
        cameraController.update(
            player.getX() * size,
            player.getY() * size,
            viewport.getWorldWidth(),
            viewport.getWorldHeight(),
            map.getMapWidth() * size,
            map.getMapHeight() * size
        );

        mapCamera.update();
        // Render the map on the screen
        renderMap();
        // Render the HUD on the screen
        hud.render(totalTime);

        // Sync exit state from Logic to Map
        if (gameState.isExitUnlocked() && !map.isExitUnlocked()) {
            map.setExitUnlocked(true);
            System.out.println("[INFO] Map exit unlocked (synced).");
        }
    }
    
    
    /**
     * Clears the screen and draws all map objects in their specific order.
     */
    private void renderMap() {
        // This configures the spriteBatch to use the camera's perspective when rendering
        spriteBatch.setProjectionMatrix(mapCamera.combined);
        
        // Start drawing
        spriteBatch.begin();

        //Get current player coordinates and define a rendering radius
        int playerX = Math.round(player.getX());
        int playerY = Math.round(player.getY());
        int radius = 30;
        // Render everything in the map here, in order from lowest to highest (later things appear on top)
        // You may want to add a method to GameMap to return all the drawables in the correct order

        //Render background layer
        int size = TILE_SIZE_PX * SCALE;
        for (int x = playerX - radius; x <= playerX + radius; x++) {
            for (int y = playerY - radius; y <= playerY + radius; y++) {
                spriteBatch.draw(Animations.GRASS, x * size, y * size, size, size);
            }
        }

        // draw crop
        for (Crop crop : map.getCrops()) {
            draw(spriteBatch, crop);
        }

        for (int x = playerX - radius; x <= playerX + radius; x++) {
            for (int y = playerY - radius; y <= playerY + radius; y++) {
                int t = map.getCellType(x, y);

                if (t == 0) {
                    spriteBatch.draw(Animations.FENCE, x * size, y * size, size, size);
                } else if (t == 1) {
                    spriteBatch.draw(Animations.DEBRIS, x * size, y * size, size, size);
                } else if (t == 2) {
                    spriteBatch.draw(Animations.ENTRANCE, x * size, y * size, size, size);
                }else if (t == 4) {
                    if (map.isExitUnlocked()) {
                        spriteBatch.draw(Animations.EXITOPEN, x * size, y * size, size, size);
                    } else {
                        spriteBatch.draw(Animations.EXITCLOSE, x * size, y * size, size, size);
                    }
                }
            }
        }
        
        Shovel shovel = map.getShovel();
            if (shovel != null && !shovel.isPickedUp()) {
                draw(spriteBatch, shovel);
            }

        Fertilizer f = map.getFertilizer();
            if (f != null && !f.isPickedUp()) {
                draw(spriteBatch, f);
            }
        WateringCan can = map.getWateringCan();
            if (can != null && !can.isPickedUp()) {
                draw(spriteBatch, can);
            }

        draw(spriteBatch, map.getPlayer());

        for (WildAnimal1 a : map.getAnimals1()) {
            draw(spriteBatch, a);
        }
        for (WildAnimal2 b : map.getAnimals2()) {
            draw(spriteBatch, b);
        }
        spriteBatch.end();
    }
    
    /**
     * Draws this object on the screen.
     * The texture will be scaled by the game scale and the tile size.
     * This should only be called between spriteBatch.begin() and spriteBatch.end(), e.g. in the renderMap() method.
     * @param spriteBatch The SpriteBatch to draw with.
     */
    private static void draw(SpriteBatch spriteBatch, Drawable drawable) {
        TextureRegion texture = drawable.getCurrentAppearance();
        // Drawable coordinates are in tiles, so we need to scale them to pixels
        float x = drawable.getX() * TILE_SIZE_PX * SCALE;
        float y = drawable.getY() * TILE_SIZE_PX * SCALE;
        // Additionally scale everything by the game scale
        float width = texture.getRegionWidth() * SCALE;
        float height = texture.getRegionHeight() * SCALE;
        if (drawable instanceof Player) {
            x -= (TILE_SIZE_PX * SCALE) * 0.5f;
            y -= (TILE_SIZE_PX * SCALE) * 0.5f;
        }
        spriteBatch.draw(texture, x, y, width, height);
    }

    /**
     * Checks if the player is on the same tile as the shovel and picks it up if possible.
     * * <p>If picked up, the shovel is added to the player's inventory and removed from the map.</p>
     */
    private void checkShovelPickup() {
        Shovel shovel = map.getShovel();
        if (shovel == null || shovel.isPickedUp()) return;

        int playerTileX = (int) player.getX();
        int playerTileY = (int) player.getY();
        int shovelTileX = (int) shovel.getX();
        int shovelTileY = (int) shovel.getY();

        if (playerTileX == shovelTileX && playerTileY == shovelTileY) {
            shovel.pickup(player);
            map.removeShovel(); 
            SoundEffect.PICKUP.play();
        }
    }

    /**
     * Checks for collision between the player and the fertilizer item.
     * * <p>When collected, it triggers a map-wide growth boost via {@code f.pickup}.</p>
     */
    private void checkFertilizerPickup() {
        Fertilizer f = map.getFertilizer();
        if (f == null || f.isPickedUp()) return;

        int px = (int) player.getX();
        int py = (int) player.getY();
        int fx = (int) f.getX();
        int fy = (int) f.getY();

        if (px == fx && py == fy) {
            f.pickup(player, map);
            map.removeFertilizer();
            SoundEffect.PICKUP.play();
        }
    }
    
    /**
     * Handles the logic for picking up the watering can.
     * * <p>The player must be standing on the exact tile coordinates of the watering can.</p>
     */
    private void checkWateringCanPickup() {
        WateringCan can = map.getWateringCan();
        if (can == null || can.isPickedUp()) return;

        int px = (int) player.getX();
        int py = (int) player.getY();
        if (px == (int) can.getX() && py == (int) can.getY()) {
            can.pickup(player, map);
            map.removeWateringCan();
            SoundEffect.PICKUP.play();
        }
    }

    /**
     * Called when the window is resized.
     * This is where the camera is updated to match the new window size.
     * @param width The new window width.
     * @param height The new window height.
     */
    @Override
    public void resize(int width, int height) {
        //Update viewport 
        viewport.update(width,height,true);
        mapCamera.setToOrtho(false);
        hud.resize(width, height);
    }

    // Unused methods from the Screen interface
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
    MusicTrack.BACKGROUND.play();
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}
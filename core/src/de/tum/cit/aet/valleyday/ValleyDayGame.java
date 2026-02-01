package de.tum.cit.aet.valleyday;

import java.io.File;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.tum.cit.aet.valleyday.audio.MusicTrack;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.screen.GameScreen;
import de.tum.cit.aet.valleyday.screen.MenuScreen;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

/**
 * The ValleyDayGame class represents the core of the Valley Day game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class ValleyDayGame extends Game {

    /**
     * Sprite Batch for rendering game elements.
     * This eats a lot of memory, so we only want one of these.
     */
    private SpriteBatch spriteBatch;

    /** The game's UI skin. This is used to style the game's UI elements. */
    private Skin skin;
    
    /**
     * The file chooser for loading map files from the user's computer.
     * This will give you access to a {@link com.badlogic.gdx.files.FileHandle} object,
     * which you can use to read the contents of the map file as a String, and then parse it into a {@link GameMap}.
     */
    private final NativeFileChooser fileChooser;
    
    /**
     * The map. This is where all the game objects are stored.
     * This is owned by {@link ValleyDayGame} and not by {@link GameScreen}
     * because the map should not be destroyed if we temporarily switch to another screen.
     */
    private GameMap map;

    /**
     * Constructor for ValleyDayGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public ValleyDayGame(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     * During the class constructor, libGDX is not fully initialized yet.
     * Therefore this method serves as a second constructor for the game,
     * and we can use libGDX resources here.
     */
    @Override
    public void create() {
        this.spriteBatch = new SpriteBatch(); // Create SpriteBatch for rendering
        this.skin = new Skin(Gdx.files.internal("skin/craftacular/craftacular-ui.json")); // Load UI skin
        this.map = new GameMap(this); // Create a new game map (you should change this to load the map from a file instead)
        MusicTrack.BACKGROUND.play(); // Play some background music
        goToMenu(); // Navigate to the menu screen
    }

    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        this.setScreen(new MenuScreen(this,null)); // Set the current screen to MenuScreen
    }

    /**
     * Switches to the game screen.
     */
    public void goToGame() {
        System.out.println("Current Directory: " + Gdx.files.internal("maps").file().getAbsolutePath());
        NativeFileChooserConfiguration config = new NativeFileChooserConfiguration();

        String path = new File("desktop/assets/maps").getAbsolutePath();
        config.directory = Gdx.files.absolute(path);

        fileChooser.chooseFile(config, new NativeFileChooserCallback() {
            @Override
            public void onFileChosen(FileHandle file) {
                map = new GameMap(ValleyDayGame.this);
                
                map.loadFromProperties(file); 
                //切入游戏画面
                Gdx.app.postRunnable(() -> setScreen(new GameScreen(ValleyDayGame.this)));
            }

            @Override
            public void onCancellation() {
                // The player canceled, did nothing, and remained in the menu
            }

            @Override
            public void onError(Exception e) {
                Gdx.app.error("ValleyDayGame", "Error choosing file: " + e.getMessage());
            }
        });
    }

    /** 
     * Returns the skin for UI elements. 
     * @return The UI skin resource.
     */
    public Skin getSkin() {
        return skin;
    }

    /** 
     * Returns the main SpriteBatch for rendering. 
     * @return The global sprite batch.
     */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
    
    /** 
     * Returns the current map, if there is one. 
     * @return The active GameMap.
     */
    public GameMap getMap() {
        return map;
    }
    
    /**
     * Switches to the given screen and disposes of the previous screen.
     * @param screen the new screen
     */
    @Override
    public void setScreen(Screen screen) {
        Screen previousScreen = super.screen;
        super.setScreen(screen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    /** Cleans up resources when the game is disposed. */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skin.dispose(); // Dispose the skin
    }

    
}

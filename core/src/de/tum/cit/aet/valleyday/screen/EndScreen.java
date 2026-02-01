package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.aet.valleyday.ValleyDayGame;

/**
 * The EndScreen class displays the final result of the game session.
 * <p>It provides a summary of victory or defeat and offers a way to return to the main menu.</p>
 */
public class EndScreen implements Screen {
    private final ValleyDayGame game;
    private final Stage stage;

    /**
     * Constructor for EndScreen. Sets up the UI based on the game outcome.
     * @param game The main game instance. 
     * @param win  True if the player won, false if they lost. 
     */
    public EndScreen(ValleyDayGame game, boolean win) {

        this.game = game;

        // Use an independent viewport to ensure UI remains crisp
        this.stage = new Stage(new ScreenViewport());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Determine title text and color based on the result
        String statusText = win ? "VICTORY!" : "GAME OVER";
        Color statusColor = win ? Color.GOLD : Color.RED;
        
        Label.LabelStyle labelStyle = new Label.LabelStyle(game.getSkin().getFont("font"), statusColor);
        Label titleLabel = new Label(statusText, labelStyle);
        titleLabel.setFontScale(2.5f); 

        // Menu Button: Resets the game session when clicked
        TextButton menuButton = new TextButton("Back to Menu", game.getSkin());
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Return to menu without a saved screen to prevent "Continuing" a finished game
                game.setScreen(new MenuScreen(game, null)); 
            }
        });

        table.add(titleLabel).padBottom(60).row();
        table.add(menuButton).width(250).height(60);
    }

    /**
     * Called when this screen becomes the current screen for the game.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    /**
     * Renders the end screen background and UI stage.
     * @param delta Seconds since last frame. 
     */
    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1); 
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    /**
     * Resizes the stage viewport when the window dimensions change.
     * @param width  New window width.
     * @param height New window height. 
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void hide() { 
        // Release input processor when the screen is hidden
        Gdx.input.setInputProcessor(null); }

    @Override public void pause() {}
    @Override public void resume() {}

    /**
     * Disposes of the stage and its resources to free memory.
     */
    @Override public void dispose() { stage.dispose(); }
}
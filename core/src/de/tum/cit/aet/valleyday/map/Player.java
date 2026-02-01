package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;

/**
 * Represents the player character in the game.
 * The player has a hitbox, so it can collide with other objects in the game.
 */
public class Player implements Drawable {

    /**
     * Total time elapsed since the game started. We use this for calculating the
     * player movement and animating it.
     */
    private float elapsedTime;

    private float branchClearSeconds = 1.0f;
    public float getBranchClearSeconds() {
        return branchClearSeconds;
    }

    /**
     * Permanently upgrades the player's efficiency in clearing environment obstacles.
     * <p>Reduces clear time from 1.0s to 0.5s after acquiring the Shovel.</p>
     */
    public void applyShovelUpgrade() {
        branchClearSeconds = 0.5f;
    }

    /** The player's inventory system. */
    private Inventory inventory = new Inventory();

    /** @return The inventory of the player. */
    public Inventory getInventory(){
        return inventory;
    }
    /** @return The elapsed time for animations. */
    public float getElapsedTime(){
        return elapsedTime;
    }

    // Direction Tracking 
    private String facingDirection = "DOWN"; // Default direction 

    /**
     * The Box2D hitbox of the player, used for position and collision detection.
     */
    private final Body hitbox;

    public Player(World world, float x, float y) {
        this.hitbox = createHitbox(world, x, y);
    }

    /**
     * Creates a Box2D body for the player.
     * This is what the physics engine uses to move the player around and detect
     * collisions with other bodies.
     * 
     * @param world  The Box2D world to add the body to.
     * @param startX The initial X position.
     * @param startY The initial Y position.
     * @return The created body.
     */
    private Body createHitbox(World world, float startX, float startY) {
        // BodyDef is like a blueprint for the movement properties of the body.
        BodyDef bodyDef = new BodyDef();
        // Dynamic bodies are affected by forces and collisions.
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set the initial position of the body.
        bodyDef.position.set(startX, startY);
        // Create the body in the world using the body definition.
        Body body = world.createBody(bodyDef);
        // Now we need to give the body a shape so the physics engine knows how to
        // collide with it.
        // We'll use a circle shape for the player.
        CircleShape circle = new CircleShape();
        // Give the circle a radius of 0.3 tiles (the player is 0.6 tiles wide).
        circle.setRadius(0.3f);
        // Attach the shape to the body as a fixture.
        // Bodies can have multiple fixtures, but we only need one for the player.
        body.createFixture(circle, 1.0f);
        // We're done with the shape, so we should dispose of it to free up memory.
        circle.dispose();
        // Set the player as the user data of the body so we can look up the player from
        // the body later.
        body.setUserData(this);
        return body;
    }

    private boolean isFrightened = false;

    /**
     * Puts the player into a frightened state where they lose control and run away.
     */
    public void beFrightened() {
    this.isFrightened = true;
    }

    /**
     * Updates player position, handles input, and performs collision look-ahead.
     * * <p>Movement is handled in two ways:</p>
     * <ul>
     * <li><b>Normal:</b> Calculates velocity based on input and uses {@code isWalkable} 
     * to prevent sticking to walls.</li>
     * <li><b>Frightened:</b> Uses {@code setTransform} to bypass collision logic and 
     * smoothly move the player off-screen.</li>
     * </ul>
     * * @param frameTime Time since last frame.
     * @param map The game map for walkability queries.
     */
    public void tick(float frameTime,GameMap map) {
        this.elapsedTime += frameTime;
        
        if (this.isFrightened) {
        float runSpeed = 8f; 
        
        float nextX = getX() - runSpeed * frameTime;
        this.hitbox.setTransform(nextX, getY(), 0); 
        
        this.facingDirection = "LEFT";
        
        return; 
        }

        //Set a walking speed 
        float speed = 2f;        
        float horizontalSpeed = 0;
        float verticalSpeed = 0;

        //4 directional movement control 
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            horizontalSpeed -= speed;
            facingDirection = "LEFT";
        }else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            horizontalSpeed += speed;
            facingDirection = "RIGHT";
        }else if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
            verticalSpeed += speed;
            facingDirection = "UP";
        }else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            verticalSpeed -= speed;
            facingDirection = "DOWN";
        }
         
        float nextX = getX() + horizontalSpeed * frameTime;
        float nextY = getY() + verticalSpeed * frameTime;

        float radius = 0.3f;

        if (horizontalSpeed != 0) {
            int checkX = (int)(nextX + Math.signum(horizontalSpeed) * radius);
            int checkY = (int)getY();
            if (!map.isWalkable(checkX, checkY)) {
                horizontalSpeed = 0;
            }
        }

        if (verticalSpeed != 0) {
            int checkX = (int)getX();
            int checkY = (int)(nextY + Math.signum(verticalSpeed) * radius);
            if (!map.isWalkable(checkX, checkY)) {
                verticalSpeed = 0;
            }
        }
        this.hitbox.setLinearVelocity(horizontalSpeed, verticalSpeed);
    }

    /**
     * Determines the correct texture frame based on movement, direction, and state.
     * <p>Includes logic for frightened state animation (always running).</p>
     * @return The current {@link TextureRegion} to draw.
     */
    @Override
    public TextureRegion getCurrentAppearance() {
        // Get the frame of the walk down animation that corresponds to the current
        // time.

        //isMoving
        boolean isMoving = Gdx.input.isKeyPressed(Input.Keys.LEFT) || 
                       Gdx.input.isKeyPressed(Input.Keys.RIGHT) || 
                       Gdx.input.isKeyPressed(Input.Keys.UP) || 
                       Gdx.input.isKeyPressed(Input.Keys.DOWN)||
                       isFrightened; 

        //Change character direction based on input 
        com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> animation;
        switch (facingDirection) {
        case "LEFT":
            animation = Animations.CHARACTER_WALK_LEFT;
            break;
        case "RIGHT":
            animation = Animations.CHARACTER_WALK_RIGHT;
            break;
        case "UP":
            animation = Animations.CHARACTER_WALK_UP;
            break;
        case "DOWN":
        default:
            animation = Animations.CHARACTER_WALK_DOWN;
            break;
    }
    return animation.getKeyFrame(isMoving ? this.elapsedTime : 0, true);
    }

    //Getter for Direction 
        public String getFacingDirection(){
            return facingDirection;
        }

    /**
     * Sets the player's position within the physics world.
     * Used to move the player to the entrance when loading a new map.
     * * @param x The target X-coordinate.
     * @param y The target Y-coordinate.
     */
    public void setPosition(float x, float y) {
        // In Box2D, `setTransform` is the standard method for teleporting objects
        //  The parameter 0 indicates that the rotation angle remains unchanged
        this.hitbox.setTransform(x, y, 0);
        
        // Reset speed to prevent players from carrying over momentum from the previous map into the new one.
        this.hitbox.setLinearVelocity(0, 0);
    }
    
    @Override
    public float getX() {
        // The x-coordinate of the player is the x-coordinate of the hitbox (this can
        // change every frame).
        return hitbox.getPosition().x;
    }

    @Override
    public float getY() {
        // The y-coordinate of the player is the y-coordinate of the hitbox (this can
        // change every frame).
        return hitbox.getPosition().y;
    }
}

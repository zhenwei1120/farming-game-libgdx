package de.tum.cit.aet.valleyday.map;

import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.audio.SoundEffect;
import de.tum.cit.aet.valleyday.texture.Animations;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Random;

/**
 * WildAnimal1 represents a wild animal entity that interacts with the map and player.
 * * <p>Behaviors include roaming randomly, eating mature crops, and fleeing from the player.
 */
public class WildAnimal1 implements Drawable {

    /** Logic X coordinate in grid tiles.*/
    private int tileX, tileY;
    /** Current interpolation position for smooth rendering. */
    private float renderX, renderY;

    private boolean alive = true;
    private final Random random = new Random();

    private boolean moving = false;
    private int startX, startY;

    /** Target coordinates for the current movement step. */
    private int targetX, targetY;

    /** Duration in seconds to cross one tile.*/
    private float moveDuration = 0.80f; 
    /** Progress of current movement from 0.0 to 1.0.  */
    private float moveProgress = 0.6f;    

    private float animTimer = 0f;
    private float animInterval = 0.15f; // How many seconds between frame switches
    private boolean animFlip = false;

    private float idleAfterStep = 0.10f;
    private float idleTimer = 0f;

    private Direction direction = Direction.RIGHT; // Default right

    /**
     * Enumeration of possible movement directions.
     */
    private enum Direction {
        LEFT, 
        RIGHT, 
        UP, 
        DOWN
    }

    private boolean scared = false;
    private float scaredTimer = 0f;
    private float scaredDuration = 0.40f;
    private float scaredSpeed = 6.0f;   

    private int scareFromPlayerX = 0;
    private int scareFromPlayerY = 0;

    /**
     * Constructs a wild animal at the specified tile coordinates.
     * * @param x Grid X position. 
     * @param y Grid Y position. 
     */
    public WildAnimal1(int x, int y) {
        this.tileX = x;
        this.tileY = y;
        this.renderX = x;
        this.renderY = y;
    }

    /**
     * Checks if the animal is still present on the map.
     * @return True if alive, false if it has fled. 
     */
    public boolean isAlive() { return alive; }

    public int getTileX() { return tileX; }
    public int getTileY() { return tileY; }

    /**
     * Triggers the animal's fleeing behavior away from the player's position.
     * * @param playerX Player's grid X. 
     * @param playerY Player's grid Y. 
     */
    public void scareAwayFrom(int playerX, int playerY) {
        if (!alive || scared) return;

        scared = true;
        scaredTimer = 0f;
        scareFromPlayerX = playerX;
        scareFromPlayerY = playerY;

        moving = false;
        moveProgress = 0f;
    }

    /**
     * Triggers the fleeing behavior using the animal's current position as the scare source.
     */
    public void scareAway() {
        if (!alive || scared) return;
        scared = true;
        scaredTimer = 0f;

        scareFromPlayerX = tileX;
        scareFromPlayerY = tileY;

        moving = false;
        moveProgress = 0f;
    }

    /**
     * Updates the animal's logic, movement, and interactions every frame.每帧调用一次
     * * @param deltaTime Time since last frame. 
     * @param map       Reference to the game map for collision and interaction. 
     */ 
    public void tick(float deltaTime, GameMap map) {
        if (!alive) return;

        // Fleeing Logic
        if (scared) {
            scaredTimer += deltaTime;

            int dx = tileX - scareFromPlayerX;
            int dy = tileY - scareFromPlayerY;

            if (Math.abs(dx) >= Math.abs(dy)) {
                if (dx >= 0) {
                    renderX += deltaTime * scaredSpeed;
                    direction = Direction.RIGHT;
                } else {
                    renderX -= deltaTime * scaredSpeed;
                    direction = Direction.LEFT;
                }
            } else {
                if (dy >= 0) {
                    renderY += deltaTime * scaredSpeed;
                    direction = Direction.UP;
                } else {
                    renderY -= deltaTime * scaredSpeed;
                    direction = Direction.DOWN;
                }
            }

            animTimer += deltaTime;
            if (animTimer >= 0.08f) {
                animTimer = 0f;
                animFlip = !animFlip;
            }

            if (scaredTimer >= scaredDuration) {
                alive = false;
            }
            return;
        }

        // Smooth Movement Logic
        if (moving) {
            moveProgress += deltaTime / moveDuration;
            if (moveProgress >= 1f) {
                moveProgress = 1f;
            }

            renderX = lerp(startX, targetX, moveProgress);
            renderY = lerp(startY, targetY, moveProgress);

            animTimer += deltaTime;
            if (animTimer >= animInterval) {
                animTimer = 0f;
                animFlip = !animFlip;
            }

            if (moveProgress >= 1f) {
                moving = false;
                tileX = targetX;
                tileY = targetY;
                idleTimer = 0f; 
            }
            return;
        }

        // Random Roaming and Interaction
        idleTimer += deltaTime;
        if (idleTimer < idleAfterStep) return;

        int dir = random.nextInt(4);
        int nx = tileX, ny = tileY;

        if (dir == 0) {
            ny += 1;
            direction = Direction.UP;
        } else if (dir == 1) {
            ny -= 1;
            direction = Direction.DOWN;
        } else if (dir == 2) {
            nx -= 1;
            direction = Direction.LEFT;
        } else {
            nx += 1;
            direction = Direction.RIGHT;
        }

        if (map.isBlocked(nx, ny)) return;

        if (map.getCellType(nx, ny) == 5) {
            Crop c = map.findCropAt(nx, ny);
            if (c != null && c.isMature()) {
                map.removeCropAt(nx, ny);
                SoundEffect.CHICKEN.play();
                beginMoveTo(nx, ny);
            }
            return;
        }

        if (map.isWalkable(nx, ny)) {
            beginMoveTo(nx, ny);
        }

        int px = (int) map.getPlayer().getX();
        int py = (int) map.getPlayer().getY();
        if (getTileX() == px && getTileY() == py) {
            map.onPlayerCaught();
        }
    }

    /**
     * Initializes a smooth transition to a new tile.
     * @param nx New X tile. 
     * @param ny New Y tile. 
     */
    private void beginMoveTo(int nx, int ny) {
        moving = true;
        startX = tileX;
        startY = tileY;
        targetX = nx;
        targetY = ny;

        moveProgress = 0f;
        animTimer = 0f;
        animFlip = false;
    }

    /**
     * Standard Linear Interpolation (Lerp) function.
     * $f(t) = a + (b - a) \times t$
     */
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public float getX() { return renderX; }

    @Override
    public float getY() { return renderY; }

    @Override
    public TextureRegion getCurrentAppearance() {
        if (direction == Direction.LEFT) {
            return animFlip ? Animations.ANIMALLEFT : Animations.ANIMALLEFT2;
        }
        return animFlip ? Animations.ANIMALRIGHT : Animations.ANIMALRIGHT2;
    }
}
package de.tum.cit.aet.valleyday.map;

import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.audio.SoundEffect;
import de.tum.cit.aet.valleyday.texture.Animations;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Random;

/**
 * WildAnimal2 represents a more advanced animal AI that actively hunts for mature crops.
 * <p>It uses Manhattan distance for target selection and a priority-axis movement algorithm
 * to navigate towards crops within its sight range.</p>
 */
public class WildAnimal2 implements Drawable {

    private int tileX, tileY;
    private float renderX, renderY;

    private boolean alive = true;
    private final Random random = new Random();

    private boolean moving = false;
    private int startX, startY;
    private int targetX, targetY;
    private float moveDuration = 0.80f;
    private float moveProgress = 0f;

    private float animTimer = 0f;
    private float animInterval = 0.15f;
    private boolean animFlip = false;

    private float idleAfterStep = 0.10f;
    private float idleTimer = 0f;

    private Direction direction = Direction.RIGHT;
    private enum Direction { LEFT, RIGHT, UP, DOWN }

    private boolean scared = false;
    private float scaredTimer = 0f;
    private float scaredDuration = 0.40f;
    private float scaredSpeed = 6.0f;
    private int scareFromPlayerX = 0;
    private int scareFromPlayerY = 0;

    /** Detection radius (in tiles) for finding mature crops. */
    private int sightRange = 12;

    /** The specific crop instance the animal is currently tracking. */
    private Crop currentTarget = null;

    public WildAnimal2(int x, int y) {
        this.tileX = x;
        this.tileY = y;
        this.renderX = x;
        this.renderY = y;
    }

    public boolean isAlive() { return alive; }
    public int getTileX() { return tileX; }
    public int getTileY() { return tileY; }

    public void scareAwayFrom(int playerX, int playerY) {
        if (!alive || scared) return;

        scared = true;
        scaredTimer = 0f;
        scareFromPlayerX = playerX;
        scareFromPlayerY = playerY;

        moving = false;
        moveProgress = 0f;
        currentTarget = null;
    }

    public void scareAway() {
        if (!alive || scared) return;

        scared = true;
        scaredTimer = 0f;
        scareFromPlayerX = tileX;
        scareFromPlayerY = tileY;

        moving = false;
        moveProgress = 0f;
        currentTarget = null;
    }

    /**
     * Updates the entity's position, animation, and state every frame.
     *
     * <p>This method handles the following sequential checks:
     * 1. If the animal is currently fleeing (scared state). 
     * 2. If the animal is in the middle of a smooth tile-to-tile movement. 
     * 3. If idle, determines the next action (searching for crops or moving randomly). 
     *
     * @param deltaTime Time elapsed since the last frame. 
     * @param map       The game map for checking collisions and crops. 
     */
    public void tick(float deltaTime, GameMap map) {
        if (!alive) return;

        if (scared) {
            scaredTimer += deltaTime;

            int dx = tileX - scareFromPlayerX;
            int dy = tileY - scareFromPlayerY;

            if (Math.abs(dx) >= Math.abs(dy)) {
                if (dx >= 0) { renderX += deltaTime * scaredSpeed; direction = Direction.RIGHT; }
                else { renderX -= deltaTime * scaredSpeed; direction = Direction.LEFT; }
            } else {
                if (dy >= 0) { renderY += deltaTime * scaredSpeed; direction = Direction.UP; }
                else { renderY -= deltaTime * scaredSpeed; direction = Direction.DOWN; }
            }

            animTimer += deltaTime;
            if (animTimer >= 0.08f) {
                animTimer = 0f;
                animFlip = !animFlip;
            }

            if (scaredTimer >= scaredDuration) alive = false;
            return;
        }

        if (moving) {
            moveProgress += deltaTime / moveDuration;
            if (moveProgress >= 1f) moveProgress = 1f;

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

                // Upon arrival, if mature crops are beneath your feet, consume them.
                tryEatCropAtCurrentTile(map);
            }
            return;
        }

        idleTimer += deltaTime;
        if (idleTimer < idleAfterStep) return;

        // Refresh Target
        if (currentTarget == null
                || !currentTarget.isMature()
                || manhattan(tileX, tileY, currentTarget.getTileX(), currentTarget.getTileY()) > sightRange) {
            currentTarget = findNearestMatureCropInRange(map, sightRange);
        }

        // Target in sight: Go after it
        if (currentTarget != null) {
            int tx = currentTarget.getTileX();
            int ty = currentTarget.getTileY();

            // Already in target square: Eat directly
            if (tileX == tx && tileY == ty) {
                eatCropAt(map, tileX, tileY);
                currentTarget = null;
                return;
            }

            int nx = tileX;
            int ny = tileY;

            int dx = tx - tileX;
            int dy = ty - tileY;

            // Determine the next step (prioritize the longer axis)
            if (Math.abs(dx) >= Math.abs(dy)) {
                if (dx > 0) { nx += 1; direction = Direction.RIGHT; }
                else if (dx < 0) { nx -= 1; direction = Direction.LEFT; }
            } else {
                if (dy > 0) { ny += 1; direction = Direction.UP; }
                else if (dy < 0) { ny -= 1; direction = Direction.DOWN; }
            }

            // Critical Fix: Allow entry into “mature crop grid” during tracking
            if (tryStepOrEat(map, nx, ny)) {
                checkCatchPlayer(map);
                return;
            }

            // If the first direction is impassable: Try the other axis.
            nx = tileX;
            ny = tileY;
            if (Math.abs(dx) >= Math.abs(dy)) {
                if (dy > 0) { ny += 1; direction = Direction.UP; }
                else if (dy < 0) { ny -= 1; direction = Direction.DOWN; }
            } else {
                if (dx > 0) { nx += 1; direction = Direction.RIGHT; }
                else if (dx < 0) { nx -= 1; direction = Direction.LEFT; }
            }

            if (tryStepOrEat(map, nx, ny)) {
                checkCatchPlayer(map);
                return;
            }

            // Neither approach works: abandon the goal and revert to random movement.
            currentTarget = null;
        }

        doRandomStep(map);
        checkCatchPlayer(map);
    }

    /**
     * Attempts to either move to a tile or interact with a crop at that location.
     * * <p>If the tile contains a mature crop, the animal eats it and then moves to that tile.
     * Otherwise, it checks if the tile is walkable.</p>
     * * @param map The game map reference. 
     * @param nx Target grid X. 
     * @param ny Target grid Y. 
     * @return true if a movement or action was successfully initiated. 
     */
    private boolean tryStepOrEat(GameMap map, int nx, int ny) {
        if (map.isBlocked(nx, ny)) return false;

        // Crop Block: Only consumes mature crops (and allows passage)
        if (map.getCellType(nx, ny) == 5) {
            Crop c = map.findCropAt(nx, ny);
            if (c != null && c.isMature()) {
                eatCropAt(map, nx, ny);     // Eat first (will revert the grid back to empty space)
                beginMoveTo(nx, ny);        // Walk over there
                currentTarget = null;
                return true;
            }
            return false;
        }

        if (map.isWalkable(nx, ny)) {
            beginMoveTo(nx, ny);
            return true;
        }

        return false;
    }

    /**
     * Executes a random movement step in one of the four cardinal directions.
     * * <p>It utilizes {@link #tryStepOrEat(GameMap, int, int)} to handle collision and interaction logic.</p>
     * @param map The game map reference. 
     */
    private void doRandomStep(GameMap map) {
        int dir = random.nextInt(4);
        int nx = tileX, ny = tileY;

        if (dir == 0) { ny += 1; direction = Direction.UP; }
        else if (dir == 1) { ny -= 1; direction = Direction.DOWN; }
        else if (dir == 2) { nx -= 1; direction = Direction.LEFT; }
        else { nx += 1; direction = Direction.RIGHT; }

        tryStepOrEat(map, nx, ny);
    }

    /**
     * Scans the map for the closest mature crop within sight range.
     * * @param range The max distance to scan. 
     * @return The nearest mature crop, or null if none found. / 
     */
    private Crop findNearestMatureCropInRange(GameMap map, int range) {
        Crop best = null;
        int bestDist = Integer.MAX_VALUE;

        for (Crop c : map.getCrops()) {
            if (c == null || !c.isMature()) continue;

            int cx = c.getTileX();
            int cy = c.getTileY();
            int dist = manhattan(tileX, tileY, cx, cy);

            if (dist <= range && dist < bestDist) {
                bestDist = dist;
                best = c;
            }
        }
        return best;
    }

    /**
     * Calculates the Manhattan distance between two points.
     * $d = |x_1 - x_2| + |y_1 - y_2|$
     */
    private int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /**
     * Checks if the tile the animal just arrived at contains a mature crop.
     * * <p>This ensures that if an animal roams onto a crop without the "hunting" trigger, 
     * it still interacts with it.</p>
     * @param map The game map reference. 
     */
    private void tryEatCropAtCurrentTile(GameMap map) {
        if (map.getCellType(tileX, tileY) != 5) return;
        Crop c = map.findCropAt(tileX, tileY);
        if (c != null && c.isMature()) {
            eatCropAt(map, tileX, tileY);
            currentTarget = null;
        }
    }

    /**
     * Executes the interaction when an animal eats a crop.
     * <p>Removes the crop, resets the tile type, and plays a sound effect.</p>
     * @param map The game map reference. 
     * @param x Grid X position.
     * @param y Grid Y position.
     */
    private void eatCropAt(GameMap map, int x, int y) {
        map.removeCropAt(x, y);
        map.setCellType(x, y, 3);   // 吃完变回空地
        SoundEffect.BIRD.play();
    }

    /**
     * Checks if the animal has collided with the player.
     */
    private void checkCatchPlayer(GameMap map) {
        int px = (int) map.getPlayer().getX();
        int py = (int) map.getPlayer().getY();
        if (getTileX() == px && getTileY() == py) {
            map.onPlayerCaught();
        }
    }

    /**
     * Initializes the smooth interpolation process to move from the current tile to a new one.
     * * <p>Sets the start and target coordinates and resets movement progress and animations.</p>
     * @param nx New X tile coordinate. 
     * @param ny New Y tile coordinate. 
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
     * Standard Linear Interpolation (Lerp) formula.
     * * <p>Used to calculate the intermediate render position based on movement progress.</p>
     * <p>Formula: $f(t) = a + (b - a) \times t$</p>
     * * @param a Start value.
     * @param b End value. 
     * @param t Progress factor (0.0 to 1.0). 。
     * @return The interpolated value.
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
        return animFlip ? Animations.ENGEL1 : Animations.ENGEL2;
    }
}

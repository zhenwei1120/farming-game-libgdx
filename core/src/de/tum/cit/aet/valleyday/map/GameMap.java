package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.audio.SoundEffect;

import com.badlogic.gdx.files.FileHandle;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import com.badlogic.gdx.utils.Array;

import java.util.*;

/**
 * Represents the game map.
 * Holds all the objects and entities in the game.
 */
public class GameMap {
    
    private final List<Crop> crops = new ArrayList<>();
    private final List<WildAnimal1> animals1 = new ArrayList<>();
    private final List<WildAnimal2> animals2 = new ArrayList<>();
    private static final int CHICKEN_COUNT = 3;
    private static final int SNAIL_COUNT = 1;
    private Shovel shovel;
    private Fertilizer fertilizer;
    private WateringCan wateringCan;

    public List<Crop> getCrops() {
        return crops;
    }
    public List<WildAnimal1> getAnimals1() {
        return animals1;
    }
    public List<WildAnimal2> getAnimals2() {
        return animals2;
    }
        
    // A static block is executed once when the class is referenced for the first time.
    static {
        // Initialize the Box2D physics engine.
        com.badlogic.gdx.physics.box2d.Box2D.init();
    }
    
    // Box2D physics simulation parameters (you can experiment with these if you want, but they work well as they are)
    /**
     * The time step for the physics simulation.
     * This is the amount of time that the physics simulation advances by in each frame.
     * It is set to 1/refreshRate, where refreshRate is the refresh rate of the monitor, e.g., 1/60 for 60 Hz.
     */
    private static final float TIME_STEP = 1f / Gdx.graphics.getDisplayMode().refreshRate;
    /** The number of velocity iterations for the physics simulation. */
    private static final int VELOCITY_ITERATIONS = 6;
    /** The number of position iterations for the physics simulation. */
    private static final int POSITION_ITERATIONS = 2;

    /**
     * The accumulated time since the last physics step.
     * We use this to keep the physics simulation at a constant rate even if the frame rate is variable.
     */
    private float physicsTime = 0;
    
    /** The game, in case the map needs to access it. */
    private final ValleyDayGame game;
    /** The Box2D world for physics simulation. */
    private final World world;
    
    // Game objects
    private final Player player;

    //Map size, number of tiles
    private int mapWidth = 21;
    private int mapHeight = 21;
  
    private int[][] cellType;
    private Body[][] debrisBodies;

    // Exit / Gate 
    private int exitX = -1;
    private int exitY = -1;
    private boolean exitRevealed = false;
    private boolean exitUnlocked = false;

    private static final int FENCE = 0;
    private static final int DEBRIS = 1;
    private static final int ENTRANCE = 2;
    private static final int GROUND = 3;
    private static final int EXIT = 4;
    private static final int CROP = 5;
    
    private final Map<String, Integer> hiddenObjects = new HashMap<>();

    private boolean gameOver = false;


    public GameMap(ValleyDayGame game) {
        this.game = game;
        this.world = new World(Vector2.Zero, true);

        //read the map
        this.cellType = new int[mapWidth][mapHeight];

        //By default, all surfaces are passable
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                cellType[x][y] = 3;
            }
        }

        //loadCellTypes("maps/map-1.properties");
        //initExitOrRandomThenHide();
        debrisBodies = new Body[mapWidth][mapHeight];

        createBlockingColliders();
        
        //The player's spawn point originates from the tile with value = 2 in the map file.
        int[] entrance = findEntrance();
        this.player = new Player(this.world, entrance[0] + 0.5f, entrance[1] + 0.5f);
    }
    
    /**
     * Updates the game state. This is called once per frame.
     * Every dynamic object in the game should update its state here.
     * @param frameTime the time that has passed since the last update
     */
    public void tick(float frameTime) {
        this.player.tick(frameTime,this);
        doPhysicsStep(frameTime);
        player.tick(frameTime, this);
        world.step(frameTime, 6, 2);
        
        for (Crop c : crops) {
            c.tick(frameTime);
        }
        for (WildAnimal1 a : animals1) {
            a.tick(frameTime, this);
        }
        animals1.removeIf(a -> !a.isAlive());

        for (WildAnimal2 b : animals2) {
            b.tick(frameTime, this);
        }
        animals2.removeIf(b -> !b.isAlive());
    }
    
    /**
     * Performs as many physics steps as necessary to catch up to the given frame time.
     * This will update the Box2D world by the given time step.
     * @param frameTime Time since last frame in seconds
     */
    private void doPhysicsStep(float frameTime) {
        this.physicsTime += frameTime;
        while (this.physicsTime >= TIME_STEP) {
            this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            this.physicsTime -= TIME_STEP;
        }
    }

    /** Creates static Box2D bodies for blocking tiles (0 = wall, 1 = destructible wall). */
    private void createBlockingColliders() {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                int t = cellType[x][y];
                if (t == 0 || t == 1) {
                    BodyDef bodyDef = new BodyDef();
                    bodyDef.type = BodyDef.BodyType.StaticBody;
                    // Place collider at center of tile
                    bodyDef.position.set(x + 0.5f, y + 0.5f);

                    Body body = world.createBody(bodyDef);

                    PolygonShape box = new PolygonShape();
                    box.setAsBox(0.45f, 0.45f);
                    body.createFixture(box, 1.0f);
                    box.dispose();

                    // Optional: tag for future (e.g., removing t==1 later)
                    body.setUserData("BLOCK_" + t);

                    if (t == 1) {
                    debrisBodies[x][y] = body;//If it's debris, store the body
                    }
                }
            }
        }
    }

    /**
     * Parses the map layout from a properties file and initializes the grid.
     * <p>Assigns special tiles like entrance, hidden objects, and randomizes the exit 
     * position if not explicitly defined in the file.</p>
     * * @param file The {@link FileHandle} to the map properties file. 
     */
    public void loadFromProperties(FileHandle file) {
        String text = file.readString();
        List<String> debrisCoords = new ArrayList<>();
        boolean exitDefined = false;

        // clear the old data
        hiddenObjects.clear();
        for (int x = 0; x < mapWidth; x++) Arrays.fill(cellType[x], GROUND);

        for (String line : text.split("\\r?\\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("=");
            String[] xy = parts[0].trim().split(",");
            int x = Integer.parseInt(xy[0].trim());
            int y = Integer.parseInt(xy[1].trim());
            int value = Integer.parseInt(parts[1].trim());

            if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) continue;

            if (value >= 4 && value <= 7) { 
                // Note 1: Items (4, 5, 6, 7) initially disguise themselves as DEBRIS(1).
                cellType[x][y] = DEBRIS; 
                hiddenObjects.put(x + "," + y, value);
                if (value == EXIT) {
                    exitX = x; exitY = y;
                    exitDefined = true;
                }
            } else if (value == ENTRANCE) {
                cellType[x][y] = ENTRANCE;
                // Update the positions of existing players directly
                if (player != null) player.setPosition(x + 0.5f, y + 0.5f);
            } else {
                cellType[x][y] = value;
                if (value == DEBRIS) debrisCoords.add(x + "," + y);
            }
        }

        // random exit
        if (!exitDefined && !debrisCoords.isEmpty()) {
            String randomLoc = debrisCoords.get(new Random().nextInt(debrisCoords.size()));
            hiddenObjects.put(randomLoc, EXIT);
            String[] split = randomLoc.split(",");
            exitX = Integer.parseInt(split[0]);
            exitY = Integer.parseInt(split[1]);
        }
        // After loading the data, regenerate the physical collision bodies.
        refreshPhysicsColliders();
    }

        /** Finds the entrance cell (value 2). Returns {x, y}. */
        private int[] findEntrance() {
            for (int x = 0; x < mapWidth; x++) {
                for (int y = 0; y < mapHeight; y++) {
                    if (cellType[x][y] == 2) {
                        return new int[]{x, y};
                    }
                }
            }
            // fallback if map has no entrance
            return new int[]{1, 3};
        }

        /**
         * Clears debris at specific tile coordinates and reveals hidden objects.
         * <p>If a hidden object (like the exit or a tool) is buried here, it transforms the tile type.</p>
         *
         * @param x Grid X coordinate.
         * @param y Grid Y coordinate.
         */
        public void clearDebris(int x, int y) {
        if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) return;
        if (cellType[x][y] != DEBRIS) return;

        if (debrisBodies[x][y] != null) {
            world.destroyBody(debrisBodies[x][y]);
            debrisBodies[x][y] = null;
        }

        // Check for hidden items
        String key = x + "," + y;
        if (hiddenObjects.containsKey(key)) {
            int realType = hiddenObjects.get(key);
            cellType[x][y] = realType; // Return to the actual item (exit/wateringcan, etc.)
            if (realType == EXIT) {
                exitRevealed = true;
                System.out.println("Exit found at " + x + "," + y);
            }
            hiddenObjects.remove(key); // After removal, delete it from the hidden repository
        } else {
            cellType[x][y] = GROUND; // After clearing ordinary debris, the area becomes vacant land
        }
    }

        /**
         * Checks if a tile is occupied by a permanent or temporary obstacle.
         * 
         * @param x Grid X coordinate.
         * @param y Grid Y coordinate.
         * @return true if the tile is a {@code FENCE} or {@code DEBRIS}.
         */
        public boolean isBlocked(int x, int y){
        if(x < 0 || x >= mapWidth || y < 0 || y >= mapHeight){
            return true;
        }
        int t = cellType[x][y];
        // If it is a fence (0), DEBRIS (1), or crop (5), return true to indicate it is blocked
        return t == FENCE || t == DEBRIS ;
    }

        /**
         * Manually updates the tile type at a specific location.
         * @param x Grid X coordinate.
         * @param y Grid Y coordinate.
         * @param type The integer ID of the cell type.
         */
        public void setCellType(int x, int y, int type) {
        if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
            this.cellType[x][y] = type;
        }
    }

        /**
         * Determines if an entity can enter the specified tile.
         * <p>A tile is walkable if it is not blocked and does not currently contain a crop.</p>
         * @return true if the path is clear.
         */
        public boolean isWalkable(int x, int y) {
        return !isBlocked(x, y) && !hasCropAt(x, y);
    }

        /**
         * Attempts to spawn a shovel on a random ground tile if the player doesn't already have one.
         */
        public void spawnShovelIfNeeded() {
            // Player already has a shovel: No longer spawns
            if (player.getInventory().getHasShovel()) return;
            // There's already one on the ground: no need to pick it up
            if (shovel != null && !shovel.isPickedUp()) return;

            List<int[]> candidates = new ArrayList<>();
            // The simplest way to determine “inside walls”: not along the perimeter
            for (int x = 1; x < mapWidth - 1; x++) {
                for (int y = 1; y < mapHeight - 1; y++) {
                    // Only brush on walkable surfaces 3 (will also become 3 after clearing debris)
                    if (cellType[x][y] == 3) {
                        candidates.add(new int[]{x, y});
                    }
                }
            }
            if (candidates.isEmpty()) return;
            int[] pos = candidates.get(new Random().nextInt(candidates.size()));
            shovel = new Shovel(pos[0], pos[1]);
        }

        /**
         * Attempts to spawn a fertilizer on a random ground tile if the player doesn't already have one.
         */
        public void spawnFertilizerIfNeeded() {
            // Player already has a Fertilizer: No longer spawns
            if (player.getInventory().getHasFertilizer()) return;
            // There's already one on the ground: no need to pick it up
            if (fertilizer != null && !fertilizer.isPickedUp()) return;

            List<int[]> candidates = new ArrayList<>();
            for (int x = 1; x < mapWidth - 1; x++) {
                for (int y = 1; y < mapHeight - 1; y++) {
                    if (cellType[x][y] == 3) {
                        candidates.add(new int[]{x, y});
                    }
                }
            }

            if (candidates.isEmpty()) return;
            int[] pos = candidates.get(new Random().nextInt(candidates.size()));
            fertilizer = new Fertilizer(pos[0], pos[1]);
        }

        /**
         * Attempts to spawn a watercan on a random ground tile if the player doesn't already have one.
         */
        public void spawnWateringCanIfNeeded() {
            if (player.getInventory().getHasWateringCan()) return;
            if (wateringCan != null && !wateringCan.isPickedUp()) return;

            List<int[]> candidates = new ArrayList<>();
            for (int x = 1; x < mapWidth - 1; x++) {
                for (int y = 1; y < mapHeight - 1; y++) {
                    if (cellType[x][y] == 3) candidates.add(new int[]{x, y});
                }
            }
            if (candidates.isEmpty()) return;

            int[] pos = candidates.get(new Random().nextInt(candidates.size()));
            wateringCan = new WateringCan(pos[0], pos[1]);
        }

        /**
         * Instantly advances all currently planted crops by one growth stage.
         * <p>Typically triggered by the Fertilizer item.</p>
         */
        public void fertilizeAllCropsInstantly() {
            for (Crop c : crops) {
                c.fertilizeAdvanceOneStage();
            }
        }

        /**
         * Revives all rotten crops and extends their mature duration globally.
         * <p>Triggered when using the Watering Can tool.</p>
         */
        public void applyWateringCanGlobalEffect() {
            for (Crop c : crops) {
                c.reviveRottenToMatureAndExtend(60f);
            }
        }

        /**
         * Initializes the exit location. If no exit is defined in the map file, 
         * a random valid ground tile is selected. The exit is initially hidden as debris.
         */
        private void initExitOrRandomThenHide() {
            // First check if there is an exit in the map file (cellType == 4)
            for (int x = 0; x < mapWidth; x++) {
                for (int y = 0; y < mapHeight; y++) {
                    if (cellType[x][y] == EXIT) {
                        exitX = x;
                        exitY = y;
                        break;
                    }
                }
            }

            // If none exists, randomly select a ground surface within the city walls
            if (exitX == -1) {
                int[] entrance = findEntrance();
                Random r = new Random();

                for (int tries = 0; tries < 500; tries++) {
                    int x = 1 + r.nextInt(mapWidth - 2);
                    int y = 1 + r.nextInt(mapHeight - 2);

                    // Does not coincide with the entrance
                    if (x == entrance[0] && y == entrance[1]) continue;

                    if (cellType[x][y] != GROUND) continue;

                    exitX = x;
                    exitY = y;
                    break;
                }
            }

            // Regardless of whether the map specifies an exit: Start by disguising yourself as gravel (1) to avoid being spotted.
            if (exitX != -1) {
                cellType[exitX][exitY] = DEBRIS;
                exitRevealed = false;
            }
        }

        /**
         * Checks if there is a {@link Crop} instance registered at the given coordinates.
         */
        public boolean hasCropAt(int x, int y) {
        for (Crop c : crops) {
            if (c.getTileX() == x && c.getTileY() == y) {
                return true;
            }
        }
        return false;
    }

        /**
         * Dynamically spawns animals to maintain the required population counts.
         * <p>Ensures animals spawn on walkable tiles and at a safe distance from the player.</p>
         */
        public void spawnAnimalIfNeeded() {
        // clear out the dead ones
        animals1.removeIf(a -> !a.isAlive());
        animals2.removeIf(b -> !b.isAlive());

        Random r = new Random();
        int px = Math.round(player.getX());
        int py = Math.round(player.getY());

        // Add more chicken
        while (animals1.size() < CHICKEN_COUNT) {
            boolean spawned = false;

            for (int tries = 0; tries < 200; tries++) {
                int x = 1 + r.nextInt(mapWidth - 2);
                int y = 1 + r.nextInt(mapHeight - 2);

                int dist = Math.abs(x - px) + Math.abs(y - py);
                if (dist < 5) continue;

                if (!isWalkable(x, y)) continue;

                if (hasAnimal1At(x, y)) continue;
                if (hasAnimal2At(x, y)) continue;

                animals1.add(new WildAnimal1(x, y));
                spawned = true;
                break;
            }

            // Stop if the location cannot be found
            if (!spawned) break;
        }

        while (animals2.size() < SNAIL_COUNT) {
            boolean spawned = false;

            for (int tries = 0; tries < 200; tries++) {
                int x = 1 + r.nextInt(mapWidth - 2);
                int y = 1 + r.nextInt(mapHeight - 2);

                int dist = Math.abs(x - px) + Math.abs(y - py);
                if (dist < 5) continue;

                if (!isWalkable(x, y)) continue;

                if (hasAnimal1At(x, y)) continue;
                if (hasAnimal2At(x, y)) continue;

                animals2.add(new WildAnimal2(x, y));
                spawned = true;
                break;
            }

            if (!spawned) break;
        }
    }

        /**
         * Checks for the presence of a specific animal type at the given tile.
         * @param x Grid X coordinate.
         * @param y Grid Y coordinate.
         * @return true if an alive animal of this type is at the location.
         */
        public boolean hasAnimal1At(int x, int y) {
            for (WildAnimal1 a : animals1) {
                if (a.isAlive() && a.getTileX() == x && a.getTileY() == y) {
                    return true;
                }
            }
            return false;
        }
        public boolean hasAnimal2At(int x, int y) {
            for (WildAnimal2 b : animals2) {
                if (b.isAlive() && b.getTileX() == x && b.getTileY() == y) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Finds and returns the {@link Crop} instance at the specified coordinates.
         * @return The {@code Crop} object, or {@code null} if none exists at that tile.
         */
        public Crop findCropAt(int x, int y) {
            for (Crop c : crops) {
                if (c.getTileX() == x && c.getTileY() == y) return c;
            }
            return null;
        }

        /**
         * Removes a crop from the map and resets the tile to {@code GROUND}.
         */
        public void removeCropAt(int x, int y) {
            Crop c = findCropAt(x, y);
            if (c != null) {
                crops.remove(c);
                setCellType(x, y, GROUND);
            }
        }

    private boolean isEnding = false;

    /**
         * Triggers the lose-game sequence when an animal catches the player.
         * <p>Initiates a frightened animation for the player and sets {@code gameOver} after a delay.</p>
         */
    public void onPlayerCaught() {
            if (this.isEnding) return;
            this.isEnding = true;

            //Play sound effect
            SoundEffect.SCARE.play(); 

            // Switch player to “Panic Run” mode
            player.beFrightened(); 

            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    gameOver = true;
                }
            }, 0.8f);
        }
        
        /**
         * Recreates all static Box2D bodies based on the current {@code cellType} array.
         * <p>Typically called after loading a new level to ensure colliders match the visual map.</p>
         */
        private void refreshPhysicsColliders() {
        // Destroy all old bodies (to prevent walls from old maps remaining in new maps)
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        for (Body b : bodies) {
            // Only non-player Bodies are destroyed.
            if (b.getUserData() != null && b.getUserData().toString().startsWith("BLOCK_")) {
                world.destroyBody(b);
            }
        }
        // Re-run the existing generation logic
        createBlockingColliders(); 
    }

    /** Returns the player on the map. */
    public Player getPlayer() {
        return player;
    }

    public boolean isGameOver() { 
        return gameOver; 
    }

    public int getMapWidth() { 
        return mapWidth; 
    }
    public int getMapHeight() { 
        return mapHeight; 
    }
    public int[] getEntrance() { 
        return findEntrance(); 
    }
    public int getCellType(int x, int y) {
        if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) return -1;
            return cellType[x][y];
    }
    public Shovel getShovel() {
        return shovel;
    }
    public void removeShovel() {
        shovel = null;
    }
    public Fertilizer getFertilizer() { 
        return fertilizer; 
    }
    public void removeFertilizer() { 
        fertilizer = null; 
    }
    public WateringCan getWateringCan() { 
        return wateringCan; 
    }
    public void removeWateringCan() { 
        wateringCan = null; 
    }
    public boolean isExitRevealed() { 
        return exitRevealed; 
    }
    public int getExitX() { 
        return exitX; 
    }
    public int getExitY() { 
        return exitY; 
    }
    public boolean isExitUnlocked() { 
        return exitUnlocked; 
    }
    public void setExitUnlocked(boolean unlocked) { 
        this.exitUnlocked = unlocked; 
    }
}
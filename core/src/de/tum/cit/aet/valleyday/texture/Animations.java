package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Contains all animation constants used in the game.
 * It is good practice to keep all textures and animations in constants to avoid loading them multiple times.
 * These can be referenced anywhere they are needed.
 */
public class Animations {
    /**
     * The animation for the character walking down.
     */
    public static final Animation<TextureRegion> CHARACTER_WALK_DOWN = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(1, 1),
            SpriteSheet.CHARACTER.at(1, 2),
            SpriteSheet.CHARACTER.at(1, 3),
            SpriteSheet.CHARACTER.at(1, 4)
    );
    /**
     * The animation for the character walking right.
     */
     public static final Animation<TextureRegion> CHARACTER_WALK_RIGHT = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(2, 1),
            SpriteSheet.CHARACTER.at(2, 2),
            SpriteSheet.CHARACTER.at(2, 3),
            SpriteSheet.CHARACTER.at(2, 4)
    );
    /** The animation for the character walking up. */
     public static final Animation<TextureRegion> CHARACTER_WALK_UP = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(3, 1),
            SpriteSheet.CHARACTER.at(3, 2),
            SpriteSheet.CHARACTER.at(3, 3),
            SpriteSheet.CHARACTER.at(3, 4)
    );
    /** The animation for the character walking left. */
     public static final Animation<TextureRegion> CHARACTER_WALK_LEFT = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(4, 1),
            SpriteSheet.CHARACTER.at(4, 2),
            SpriteSheet.CHARACTER.at(4, 3),
            SpriteSheet.CHARACTER.at(4, 4)
    );
    /** TextureRegion for the grass background tile. */
     public static final TextureRegion GRASS =  SpriteSheet.BASIC_TILES.at(9, 1);

     /** TextureRegion for fence (wall) tile */
     public static final TextureRegion FENCE = SpriteSheet.BASIC_TILES.at(1, 1);

     /** TextureRegion for debris (destructible bush) */
        public static final TextureRegion DEBRIS = SpriteSheet.OUTSIDE.at(3, 2);

      /** TextureRegion for Entrance */
      public static final TextureRegion ENTRANCE = SpriteSheet.BASIC_TILES.at(7, 1);
      public static final TextureRegion EXITCLOSE = SpriteSheet.BASIC_TILES.at(7, 2);
      public static final TextureRegion EXITOPEN = SpriteSheet.BASIC_TILES.at(7, 3);

      /** TextureRegion for Crop */
        public static final TextureRegion CROP_SEED    = SpriteSheet.HARVEST.at(3, 1);
        public static final TextureRegion CROP_GROW1 = SpriteSheet.HARVEST.at(3, 2);
        public static final TextureRegion CROP_GROW2 = SpriteSheet.HARVEST.at(3, 3);
        public static final TextureRegion CROP_MATURE  = SpriteSheet.HARVEST.at(3, 4);
        public static final TextureRegion CROP_ROTTEN  = SpriteSheet.HARVEST.at(3, 8);

        /** TextureRegion for Shovel */
        public static final TextureRegion SHOVEL = SpriteSheet.BASIC.at(3, 7);

        /** TextureRegion for fertilizer */
        public static final TextureRegion FERTILIZER = SpriteSheet.BASIC_TILES.at(4,4);

        /** TextureRegion for watercan */
        public static final TextureRegion WATERCAN = SpriteSheet.HARVEST.at(1, 7);

        /** TextureRegion for wildAnimal1*/
        public static final TextureRegion ANIMALRIGHT = SpriteSheet.FARMTHINGS.at(1, 1);
        public static final TextureRegion ANIMALRIGHT2 = SpriteSheet.FARMTHINGS.at(1, 2);
        public static final TextureRegion ANIMALLEFT = SpriteSheet.FARMTHINGS1.at(1, 7);
        public static final TextureRegion ANIMALLEFT2 = SpriteSheet.FARMTHINGS1.at(1, 8);

        /** TextureRegion for wildAnimal2*/
        public static final TextureRegion ENGEL1 = SpriteSheet.ENGEL1.at(1, 1);
        public static final TextureRegion ENGEL2 = SpriteSheet.ENGEL2.at(1, 1);
}
package com.asocity.bounceup166460;

public class Constants {

    // World dimensions
    public static final float WORLD_WIDTH  = 480f;
    public static final float WORLD_HEIGHT = 854f;

    // Player dimensions
    public static final float PLAYER_WIDTH  = 48f;
    public static final float PLAYER_HEIGHT = 64f;

    // Physics
    public static final float GRAVITY           = -1800f;
    public static final float JUMP_VELOCITY     = 720f;
    public static final float MAX_FALL_SPEED    = -1200f;
    public static final float TILT_SENSITIVITY  = 320f;
    public static final float MAX_HORIZONTAL_SPEED = 400f;

    // Platform dimensions
    public static final float PLATFORM_WIDTH  = 90f;
    public static final float PLATFORM_HEIGHT = 18f;

    // Platform generation
    public static final int   PLATFORM_COUNT    = 14;
    public static final float PLATFORM_GAP_MIN  = 55f;
    public static final float PLATFORM_GAP_MAX  = 115f;

    // Platform types (probability weights)
    public static final float PLATFORM_NORMAL_CHANCE  = 0.65f;
    public static final float PLATFORM_MOVING_CHANCE  = 0.20f;
    // remaining chance = broken

    // Moving platform speed
    public static final float PLATFORM_MOVE_SPEED = 80f;

    // Star / collectible
    public static final float STAR_SIZE  = 28f;
    public static final int   STAR_SCORE = 1;

    // Hazard (meteor / spike ball)
    public static final float HAZARD_SIZE  = 32f;
    public static final float HAZARD_SPEED = 140f;

    // Height scoring (1 unit = 1 metre)
    public static final float UNITS_PER_METER = 1f;

    // World IDs
    public static final int WORLD_SKY   = 0;
    public static final int WORLD_CAVE  = 1;
    public static final int WORLD_SPACE = 2;

    // World unlock costs (in stars)
    public static final int WORLD_CAVE_UNLOCK_COST  = 50;
    public static final int WORLD_SPACE_UNLOCK_COST = 100;

    // Skin prices (stars)
    public static final int SKIN_1_PRICE = 0;
    public static final int SKIN_2_PRICE = 100;
    public static final int SKIN_3_PRICE = 150;

    // Difficulty levels
    public static final int DIFFICULTY_EASY   = 0;
    public static final int DIFFICULTY_NORMAL = 1;
    public static final int DIFFICULTY_HARD   = 2;

    // Difficulty multipliers
    public static final float DIFFICULTY_EASY_GAP_MULT   = 0.75f;
    public static final float DIFFICULTY_HARD_GAP_MULT   = 1.35f;

    // UI sizes
    public static final float BUTTON_WIDTH            = 240f;
    public static final float BUTTON_HEIGHT           = 70f;
    public static final float BUTTON_SECONDARY_WIDTH  = 200f;
    public static final float BUTTON_SECONDARY_HEIGHT = 60f;
    public static final float ICON_BUTTON_SIZE        = 60f;
    public static final float WORLD_CARD_WIDTH        = 380f;
    public static final float WORLD_CARD_HEIGHT       = 120f;

    // Font sizes
    public static final int FONT_SIZE_TITLE  = 48;
    public static final int FONT_SIZE_BODY   = 28;
    public static final int FONT_SIZE_SMALL  = 20;
    public static final int FONT_SIZE_HUD    = 24;
    public static final int FONT_SIZE_SCORE  = 64;

    // Leaderboard
    public static final int LEADERBOARD_SIZE = 10;

    // SharedPreferences keys
    public static final String PREFS_NAME              = "GamePrefs";
    public static final String PREF_MUSIC              = "musicEnabled";
    public static final String PREF_SFX                = "sfxEnabled";
    public static final String PREF_SKIN               = "selectedSkin";
    public static final String PREF_STARS              = "totalStars";
    public static final String PREF_DIFFICULTY         = "difficulty";
    public static final String PREF_BEST_HEIGHT_SKY    = "bestHeightSky";
    public static final String PREF_BEST_HEIGHT_CAVE   = "bestHeightCave";
    public static final String PREF_BEST_HEIGHT_SPACE  = "bestHeightSpace";
    public static final String PREF_WORLD_CAVE_UNLOCKED  = "worldCaveUnlocked";
    public static final String PREF_WORLD_SPACE_UNLOCKED = "worldSpaceUnlocked";
    public static final String PREF_SKIN_2_UNLOCKED    = "skin2Unlocked";
    public static final String PREF_SKIN_3_UNLOCKED    = "skin3Unlocked";

    // Leaderboard keys (per world, prefix + rank index)
    public static final String PREF_LEADERBOARD_SKY_PREFIX   = "lbSky_";
    public static final String PREF_LEADERBOARD_CAVE_PREFIX  = "lbCave_";
    public static final String PREF_LEADERBOARD_SPACE_PREFIX = "lbSpace_";

    // Power-up SharedPreferences keys (boolean: true = queued for next run)
    public static final String PREF_POWERUP_SHIELD = "powerupShield";
    public static final String PREF_POWERUP_MAGNET = "powerupMagnet";
    public static final String PREF_POWERUP_DOUBLE = "powerupDouble";

    // Power-up prices (in stars)
    public static final int POWERUP_SHIELD_PRICE = 20;
    public static final int POWERUP_MAGNET_PRICE = 30;
    public static final int POWERUP_DOUBLE_PRICE = 50;

    // Power-up runtime parameters
    public static final float SHIELD_DURATION  = 5f;
    public static final float MAGNET_RANGE     = 150f;
    public static final float MAGNET_SPEED     = 220f;

    // Hazard spawn
    public static final float HAZARD_SPAWN_HEIGHT   = 60f;  // metres before hazards appear
    public static final float HAZARD_SPAWN_INTERVAL = 4.5f; // seconds between spawns
}

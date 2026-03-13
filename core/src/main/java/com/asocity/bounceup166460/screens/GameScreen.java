package com.asocity.bounceup166460.screens;

import com.asocity.bounceup166460.Constants;
import com.asocity.bounceup166460.MainGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Core gameplay screen — Doodle Jump-style vertical platformer.
 *
 * <p>Player bounces automatically off platforms. Steering is via the accelerometer.
 * Stars are collected for currency. Hazards (spike-balls / meteors) drift across
 * the screen and kill the player on contact. Three worlds with different visuals.
 *
 * <p>Power-ups consumed at run start:
 *   - Shield:     5 s invincibility from hazards
 *   - Star Magnet: auto-collects nearby stars for the whole run
 *   - 2× Score:   doubles stars collected
 */
public class GameScreen implements Screen {

    // ── Inner data classes ─────────────────────────────────────────────────────

    private static final class Platform {
        float x, y;
        /** 0 = normal, 1 = moving, 2 = broken */
        int   type;
        float moveDirX    = 1f;
        boolean crumbling = false;
        float crumbleTimer = 0f;
        boolean alive     = true;
    }

    private static final class Star {
        float   x, y;
        boolean collected = false;
    }

    private static final class Hazard {
        float   x, y, velX;
        boolean alive = true;
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private final MainGame game;
    private final int      worldId;

    // Rendering
    private final OrthographicCamera worldCamera;
    private final OrthographicCamera hudCamera;
    private final Viewport           hudViewport;
    private final Stage              hudStage;
    private       Label              heightLabel;
    private       Label              starsLabel;

    // Input multiplexer re-applied on show()
    private InputMultiplexer inputMultiplexer;

    // Player
    private float playerX, playerY;
    private float playerVelX, playerVelY;
    private float animTimer = 0f;
    private int   skinIndex;
    private Texture playerStandTex, playerJumpTex, playerWalkTex1, playerWalkTex2;
    private static final float PW = Constants.PLAYER_WIDTH;
    private static final float PH = Constants.PLAYER_HEIGHT;

    // Camera tracking
    private float highestPlayerY;
    private float startCameraY;
    private int   height;           // metres reached (score)
    private int   starsCollected;

    // Power-ups
    private boolean shieldActive;
    private float   shieldTimer;
    private boolean magnetActive;
    private boolean doubleScore;

    // Difficulty
    private float gapMultiplier = 1f;

    // World
    private Array<Platform> platforms = new Array<>();
    private Array<Star>     stars     = new Array<>();
    private Array<Hazard>   hazards   = new Array<>();
    private float highestGeneratedY;
    private float hazardTimer = 0f;

    // World-specific textures
    private Texture bgTex;
    private Texture platformNormalTex;
    private Texture platformMovingTex;
    private Texture platformBrokenTex;

    // Sounds (cached references after loading)
    private Sound sfxJump, sfxCoin, sfxHit, sfxGameOver, sfxClick;

    // ── Asset lists ───────────────────────────────────────────────────────────

    private static final String[] COMMON_ASSETS = {
        "sprites/jumper/player_stand.png",
        "sprites/jumper/player_walk1.png",
        "sprites/jumper/player_walk2.png",
        "sprites/jumper/player_jump.png",
        "sprites/jumper/player2_stand.png",
        "sprites/jumper/player2_walk1.png",
        "sprites/jumper/player2_walk2.png",
        "sprites/jumper/player2_jump.png",
        "sprites/jumper/platform_grass.png",
        "sprites/jumper/platform_grass_broken.png",
        "sprites/jumper/platform_stone.png",
        "sprites/jumper/platform_snow.png",
        "sprites/jumper/platform_sand.png",
        "sprites/jumper/platform_wood.png",
        "sprites/jumper/platform_wood_broken.png",
        "sprites/jumper/hazard_spikeball.png",
        "sprites/star.png",
        "sprites/icon_pause.png",
        "sprites/button_blue.png",
        "sprites/button_blue_pressed.png",
        "sounds/sfx/sfx_jump.ogg",
        "sounds/sfx/sfx_coin.ogg",
        "sounds/sfx/sfx_hit.ogg",
        "sounds/sfx/sfx_game_over.ogg",
        "sounds/sfx/sfx_button_click.ogg"
    };

    // ── Constructor ───────────────────────────────────────────────────────────

    public GameScreen(MainGame game, int worldId) {
        this.game    = game;
        this.worldId = worldId;

        // Load common assets
        for (String path : COMMON_ASSETS) {
            if (!game.manager.isLoaded(path)) {
                if (path.endsWith(".ogg"))
                    game.manager.load(path, Sound.class);
                else
                    game.manager.load(path, Texture.class);
            }
        }

        // Load world-specific assets
        String bgPath    = bgPathForWorld();
        String musicPath = musicPathForWorld();
        if (!game.manager.isLoaded(bgPath))    game.manager.load(bgPath,    Texture.class);
        if (!game.manager.isLoaded(musicPath)) game.manager.load(musicPath, Music.class);

        game.manager.finishLoading();

        // Cache sound references
        sfxJump     = game.manager.get("sounds/sfx/sfx_jump.ogg",      Sound.class);
        sfxCoin     = game.manager.get("sounds/sfx/sfx_coin.ogg",      Sound.class);
        sfxHit      = game.manager.get("sounds/sfx/sfx_hit.ogg",       Sound.class);
        sfxGameOver = game.manager.get("sounds/sfx/sfx_game_over.ogg", Sound.class);
        sfxClick    = game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class);

        // World-specific texture refs
        bgTex = game.manager.get(bgPath, Texture.class);
        switch (worldId) {
            case Constants.WORLD_CAVE:
                platformNormalTex = game.manager.get("sprites/jumper/platform_stone.png",      Texture.class);
                platformMovingTex = game.manager.get("sprites/jumper/platform_wood.png",       Texture.class);
                platformBrokenTex = game.manager.get("sprites/jumper/platform_wood_broken.png",Texture.class);
                break;
            case Constants.WORLD_SPACE:
                platformNormalTex = game.manager.get("sprites/jumper/platform_snow.png",       Texture.class);
                platformMovingTex = game.manager.get("sprites/jumper/platform_sand.png",       Texture.class);
                platformBrokenTex = game.manager.get("sprites/jumper/platform_wood_broken.png",Texture.class);
                break;
            default: // SKY
                platformNormalTex = game.manager.get("sprites/jumper/platform_grass.png",      Texture.class);
                platformMovingTex = game.manager.get("sprites/jumper/platform_wood.png",       Texture.class);
                platformBrokenTex = game.manager.get("sprites/jumper/platform_grass_broken.png",Texture.class);
        }

        // Player skin
        Preferences prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);
        skinIndex = prefs.getInteger(Constants.PREF_SKIN, 0);
        boolean useSkin2 = (skinIndex >= 1);
        playerStandTex = game.manager.get(
            useSkin2 ? "sprites/jumper/player2_stand.png" : "sprites/jumper/player_stand.png", Texture.class);
        playerJumpTex  = game.manager.get(
            useSkin2 ? "sprites/jumper/player2_jump.png"  : "sprites/jumper/player_jump.png",  Texture.class);
        playerWalkTex1 = game.manager.get(
            useSkin2 ? "sprites/jumper/player2_walk1.png" : "sprites/jumper/player_walk1.png", Texture.class);
        playerWalkTex2 = game.manager.get(
            useSkin2 ? "sprites/jumper/player2_walk2.png" : "sprites/jumper/player_walk2.png", Texture.class);

        // Difficulty
        int diff = prefs.getInteger(Constants.PREF_DIFFICULTY, Constants.DIFFICULTY_NORMAL);
        if      (diff == Constants.DIFFICULTY_EASY) gapMultiplier = Constants.DIFFICULTY_EASY_GAP_MULT;
        else if (diff == Constants.DIFFICULTY_HARD) gapMultiplier = Constants.DIFFICULTY_HARD_GAP_MULT;

        // Consume power-ups
        shieldActive = prefs.getBoolean(Constants.PREF_POWERUP_SHIELD, false);
        magnetActive = prefs.getBoolean(Constants.PREF_POWERUP_MAGNET, false);
        doubleScore  = prefs.getBoolean(Constants.PREF_POWERUP_DOUBLE, false);
        if (shieldActive || magnetActive || doubleScore) {
            prefs.putBoolean(Constants.PREF_POWERUP_SHIELD, false);
            prefs.putBoolean(Constants.PREF_POWERUP_MAGNET, false);
            prefs.putBoolean(Constants.PREF_POWERUP_DOUBLE, false);
            prefs.flush();
        }
        shieldTimer = shieldActive ? Constants.SHIELD_DURATION : 0f;

        // World camera (follows player)
        worldCamera = new OrthographicCamera();
        worldCamera.setToOrtho(false, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        worldCamera.position.x = Constants.WORLD_WIDTH / 2f;
        worldCamera.position.y = Constants.WORLD_HEIGHT / 2f;

        startCameraY = Constants.WORLD_HEIGHT / 2f;

        // HUD camera (fixed)
        hudCamera   = new OrthographicCamera();
        hudViewport = new StretchViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, hudCamera);
        hudStage    = new Stage(hudViewport, game.batch);

        buildHUD();
        setupInput();
        initWorld();

        game.playMusic(musicPath);
    }

    // ── World initialisation ──────────────────────────────────────────────────

    private void initWorld() {
        platforms.clear();
        stars.clear();
        hazards.clear();
        starsCollected = 0;
        height = 0;

        // First platform (static, normal) — player spawns on it
        Platform first = new Platform();
        first.x    = Constants.WORLD_WIDTH / 2f - Constants.PLATFORM_WIDTH / 2f;
        first.y    = 100f;
        first.type = 0;
        platforms.add(first);

        playerX    = first.x + Constants.PLATFORM_WIDTH / 2f - PW / 2f;
        playerY    = first.y + Constants.PLATFORM_HEIGHT;
        playerVelX = 0f;
        playerVelY = Constants.JUMP_VELOCITY;

        highestPlayerY  = playerY;
        highestGeneratedY = first.y;

        // Pre-generate enough platforms to fill the initial viewport
        while (highestGeneratedY < worldCamera.position.y + Constants.WORLD_HEIGHT) {
            addPlatform();
        }
    }

    /** Append one platform above highestGeneratedY using procedural rules. */
    private void addPlatform() {
        float gap = MathUtils.random(Constants.PLATFORM_GAP_MIN, Constants.PLATFORM_GAP_MAX)
                    * gapMultiplier;
        float newY = highestGeneratedY + Constants.PLATFORM_HEIGHT + gap;
        float newX = MathUtils.random(0, Constants.WORLD_WIDTH - Constants.PLATFORM_WIDTH);

        Platform p = new Platform();
        p.x = newX;
        p.y = newY;

        float roll = MathUtils.random();
        if (roll < Constants.PLATFORM_NORMAL_CHANCE) {
            p.type = 0;
            // 15% chance to spawn a star on normal platforms
            if (MathUtils.random() < 0.15f) {
                Star s = new Star();
                s.x = newX + Constants.PLATFORM_WIDTH / 2f - Constants.STAR_SIZE / 2f;
                s.y = newY + Constants.PLATFORM_HEIGHT + 4f;
                stars.add(s);
            }
        } else if (roll < Constants.PLATFORM_NORMAL_CHANCE + Constants.PLATFORM_MOVING_CHANCE) {
            p.type    = 1;
            p.moveDirX = MathUtils.randomBoolean() ? 1f : -1f;
        } else {
            p.type = 2;
        }

        platforms.add(p);
        highestGeneratedY = newY;
    }

    // ── HUD setup ─────────────────────────────────────────────────────────────

    private void buildHUD() {
        hudStage.clear();

        Label.LabelStyle hudStyle  = new Label.LabelStyle(game.fontHud,  Color.WHITE);
        Label.LabelStyle hudStyleG = new Label.LabelStyle(game.fontHud,  new Color(1f, 0.85f, 0f, 1f));

        heightLabel = new Label("HEIGHT: 0 m", hudStyle);
        starsLabel  = new Label("0", hudStyleG);

        // Pause button
        ImageButton.ImageButtonStyle pauseStyle = new ImageButton.ImageButtonStyle();
        pauseStyle.imageUp = new TextureRegionDrawable(
            game.manager.get("sprites/icon_pause.png", Texture.class));
        ImageButton pauseBtn = new ImageButton(pauseStyle);
        pauseBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.sfxEnabled) sfxClick.play(1.0f);
                game.setScreen(new PauseScreen(game, GameScreen.this, worldId));
            }
        });

        Table topBar = new Table();
        topBar.setFillParent(true);
        topBar.top().pad(16);
        topBar.add(heightLabel).expandX().left();

        // Star icon + count
        Table starGroup = new Table();
        starGroup.add(new com.badlogic.gdx.scenes.scene2d.ui.Image(
            game.manager.get("sprites/star.png", Texture.class))).size(28, 28).padRight(4);
        starGroup.add(starsLabel);
        topBar.add(starGroup).right().padRight(8);

        topBar.add(pauseBtn).size(44, 44).right();

        hudStage.addActor(topBar);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    private void setupInput() {
        inputMultiplexer = new InputMultiplexer(hudStage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    game.setScreen(new MainMenuScreen(game));
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    // ── Screen callbacks ──────────────────────────────────────────────────────

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    // ── Game loop ─────────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        animTimer += delta;

        // ── Shield timer ──
        if (shieldActive) {
            shieldTimer -= delta;
            if (shieldTimer <= 0f) shieldActive = false;
        }

        // ── Player horizontal (accelerometer) ──
        // Positive AccelerometerX = device tilted left in portrait = move character left
        float tilt = Gdx.input.getAccelerometerX();
        playerVelX = MathUtils.clamp(-tilt * Constants.TILT_SENSITIVITY,
                                     -Constants.MAX_HORIZONTAL_SPEED,
                                      Constants.MAX_HORIZONTAL_SPEED);

        // ── Gravity ──
        playerVelY += Constants.GRAVITY * delta;
        playerVelY  = Math.max(playerVelY, Constants.MAX_FALL_SPEED);

        // ── Move player ──
        playerX += playerVelX * delta;
        playerY += playerVelY * delta;

        // Horizontal screen wrap
        if (playerX + PW < 0)              playerX = Constants.WORLD_WIDTH;
        if (playerX > Constants.WORLD_WIDTH) playerX = -PW;

        // ── Platform collision (only when falling) ──
        if (playerVelY < 0) {
            Rectangle feet = new Rectangle(playerX + 4f, playerY, PW - 8f, 6f);
            for (int i = 0; i < platforms.size; i++) {
                Platform p = platforms.get(i);
                if (!p.alive || p.crumbling) continue;
                Rectangle platTop = new Rectangle(p.x, p.y + Constants.PLATFORM_HEIGHT - 4f,
                                                   Constants.PLATFORM_WIDTH, 8f);
                if (feet.overlaps(platTop)) {
                    playerY    = p.y + Constants.PLATFORM_HEIGHT;
                    playerVelY = Constants.JUMP_VELOCITY;
                    if (game.sfxEnabled) sfxJump.play(1.0f);
                    if (p.type == 2) {
                        p.crumbling = true; // start crumble
                    }
                    break;
                }
            }
        }

        // ── Update platforms ──
        for (int i = platforms.size - 1; i >= 0; i--) {
            Platform p = platforms.get(i);
            if (!p.alive) { platforms.removeIndex(i); continue; }

            if (p.type == 1) { // moving
                p.x += p.moveDirX * Constants.PLATFORM_MOVE_SPEED * delta;
                if (p.x <= 0)                                  p.moveDirX =  1f;
                if (p.x + Constants.PLATFORM_WIDTH >= Constants.WORLD_WIDTH) p.moveDirX = -1f;
            }
            if (p.crumbling) {
                p.crumbleTimer += delta;
                if (p.crumbleTimer >= 0.25f) p.alive = false;
            }
            // Remove platforms scrolled off the bottom
            float camBottom = worldCamera.position.y - Constants.WORLD_HEIGHT / 2f;
            if (p.y + Constants.PLATFORM_HEIGHT < camBottom) {
                platforms.removeIndex(i);
            }
        }

        // ── Generate more platforms ahead ──
        while (highestGeneratedY < worldCamera.position.y + Constants.WORLD_HEIGHT) {
            addPlatform();
        }

        // ── Camera: tracks player upward, never goes down ──
        if (playerY > highestPlayerY) {
            highestPlayerY = playerY;
            float targetCamY = highestPlayerY + Constants.WORLD_HEIGHT * 0.25f;
            if (targetCamY > worldCamera.position.y) {
                worldCamera.position.y = targetCamY;
            }
        }
        worldCamera.update();

        // Height in metres
        height = (int) Math.max(0f, worldCamera.position.y - startCameraY);

        // ── Star logic ──
        Rectangle playerRect = new Rectangle(playerX, playerY, PW, PH);
        float camBottom = worldCamera.position.y - Constants.WORLD_HEIGHT / 2f;

        for (int i = stars.size - 1; i >= 0; i--) {
            Star s = stars.get(i);
            if (s.collected) { stars.removeIndex(i); continue; }
            if (s.y + Constants.STAR_SIZE < camBottom) { stars.removeIndex(i); continue; }

            // Magnet: attract nearby stars
            if (magnetActive) {
                float cx = playerX + PW / 2f;
                float cy = playerY + PH / 2f;
                float sx = s.x + Constants.STAR_SIZE / 2f;
                float sy = s.y + Constants.STAR_SIZE / 2f;
                float dx = cx - sx, dy = cy - sy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < Constants.MAGNET_RANGE && dist > 1f) {
                    s.x += (dx / dist) * Constants.MAGNET_SPEED * delta;
                    s.y += (dy / dist) * Constants.MAGNET_SPEED * delta;
                }
            }

            // Collect
            if (playerRect.overlaps(new Rectangle(s.x, s.y, Constants.STAR_SIZE, Constants.STAR_SIZE))) {
                s.collected = true;
                starsCollected += doubleScore ? 2 : 1;
                if (game.sfxEnabled) sfxCoin.play(1.0f);
            }
        }

        // ── Hazard logic ──
        hazardTimer -= delta;
        if (hazardTimer <= 0f && height > Constants.HAZARD_SPAWN_HEIGHT) {
            hazardTimer = Constants.HAZARD_SPAWN_INTERVAL;
            spawnHazard();
        }

        for (int i = hazards.size - 1; i >= 0; i--) {
            Hazard h = hazards.get(i);
            if (!h.alive) { hazards.removeIndex(i); continue; }
            h.x += h.velX * delta;
            if (h.x > Constants.WORLD_WIDTH + Constants.HAZARD_SIZE || h.x < -Constants.HAZARD_SIZE) {
                h.alive = false; hazards.removeIndex(i); continue;
            }
            // Off bottom — remove silently
            if (h.y + Constants.HAZARD_SIZE < camBottom) { hazards.removeIndex(i); continue; }

            // Collision (smaller hitbox for fairness)
            Rectangle hazRect = new Rectangle(h.x + 4f, h.y + 4f,
                                               Constants.HAZARD_SIZE - 8f, Constants.HAZARD_SIZE - 8f);
            if (!shieldActive && hazRect.overlaps(playerRect)) {
                triggerGameOver();
                return;
            }
        }

        // ── Game over: fell off screen bottom ──
        if (playerY + PH < camBottom) {
            triggerGameOver();
        }

        // ── Update HUD labels ──
        heightLabel.setText("HEIGHT: " + height + " m");
        starsLabel.setText("" + starsCollected);
    }

    private void spawnHazard() {
        Hazard h = new Hazard();
        boolean fromLeft = MathUtils.randomBoolean();
        float spawnY = worldCamera.position.y
                       - Constants.WORLD_HEIGHT / 2f + MathUtils.random(80f, Constants.WORLD_HEIGHT - 80f);
        h.y    = spawnY;
        h.velX = fromLeft ? Constants.HAZARD_SPEED : -Constants.HAZARD_SPEED;
        h.x    = fromLeft ? -Constants.HAZARD_SIZE : Constants.WORLD_WIDTH;
        hazards.add(h);
    }

    private void triggerGameOver() {
        if (game.sfxEnabled) sfxGameOver.play(1.0f);
        game.setScreen(new GameOverScreen(game, height, starsCollected, worldId));
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void draw() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        float camBottom = worldCamera.position.y - Constants.WORLD_HEIGHT / 2f;

        game.batch.setProjectionMatrix(worldCamera.combined);
        game.batch.begin();

        // Background — tiled vertically so it fills any height
        float bgTileH = Constants.WORLD_HEIGHT;
        float bgStartY = camBottom - (camBottom % bgTileH);
        for (int i = 0; i < 3; i++) {
            game.batch.draw(bgTex, 0f, bgStartY + i * bgTileH,
                            Constants.WORLD_WIDTH, bgTileH);
        }

        // Platforms
        for (Platform p : platforms) {
            if (!p.alive) continue;
            Texture tex;
            if (p.crumbling)   tex = platformBrokenTex;
            else if (p.type == 1) tex = platformMovingTex;
            else if (p.type == 2) tex = platformBrokenTex;
            else               tex = platformNormalTex;

            // Slightly fade crumbling platforms
            if (p.crumbling) game.batch.setColor(1f, 1f, 1f, 1f - p.crumbleTimer / 0.25f);
            game.batch.draw(tex, p.x, p.y, Constants.PLATFORM_WIDTH, Constants.PLATFORM_HEIGHT);
            if (p.crumbling) game.batch.setColor(Color.WHITE);
        }

        // Stars
        Texture starTex = game.manager.get("sprites/star.png", Texture.class);
        for (Star s : stars) {
            if (!s.collected) {
                game.batch.draw(starTex, s.x, s.y, Constants.STAR_SIZE, Constants.STAR_SIZE);
            }
        }

        // Hazards
        Texture hazTex = game.manager.get("sprites/jumper/hazard_spikeball.png", Texture.class);
        for (Hazard h : hazards) {
            if (h.alive) {
                game.batch.draw(hazTex, h.x, h.y, Constants.HAZARD_SIZE, Constants.HAZARD_SIZE);
            }
        }

        // Player
        drawPlayer();

        game.batch.end();

        // Shield overlay — tinted glow around player
        if (shieldActive) {
            game.batch.begin();
            game.batch.setColor(0.3f, 0.6f, 1f, 0.35f);
            game.batch.draw(game.manager.get("sprites/jumper/player_stand.png", Texture.class),
                            playerX - 8f, playerY - 8f, PW + 16f, PH + 16f);
            game.batch.setColor(Color.WHITE);
            game.batch.end();
        }

        // HUD
        hudViewport.apply();
        hudStage.act(Gdx.graphics.getDeltaTime());
        hudStage.draw();
    }

    private void drawPlayer() {
        boolean inAir   = playerVelY > 10f || playerVelY < -10f;
        boolean moving  = Math.abs(playerVelX) > 20f;
        boolean facingLeft = playerVelX < 0;

        Texture tex;
        if (inAir) {
            tex = playerJumpTex;
        } else if (moving) {
            tex = ((animTimer % 0.3f) < 0.15f) ? playerWalkTex1 : playerWalkTex2;
        } else {
            tex = playerStandTex;
        }

        // Pink tint for skin 2
        if (skinIndex == 2) game.batch.setColor(1f, 0.6f, 0.8f, 1f);

        if (facingLeft) {
            game.batch.draw(tex, playerX + PW, playerY, -PW, PH);
        } else {
            game.batch.draw(tex, playerX, playerY, PW, PH);
        }

        if (skinIndex == 2) game.batch.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        hudViewport.update(width, height, true);
        worldCamera.viewportWidth  = Constants.WORLD_WIDTH;
        worldCamera.viewportHeight = Constants.WORLD_HEIGHT;
        worldCamera.update();
    }

    @Override
    public void dispose() {
        hudStage.dispose();

        // Unload common assets
        for (String path : COMMON_ASSETS) {
            if (game.manager.isLoaded(path)) game.manager.unload(path);
        }
        // Unload world-specific assets
        String bgPath    = bgPathForWorld();
        String musicPath = musicPathForWorld();
        if (game.manager.isLoaded(bgPath))    game.manager.unload(bgPath);
        if (game.manager.isLoaded(musicPath)) game.manager.unload(musicPath);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String bgPathForWorld() {
        switch (worldId) {
            case Constants.WORLD_CAVE:  return "backgrounds/bg_cave.png";
            case Constants.WORLD_SPACE: return "backgrounds/bg_space.png";
            default:                    return "backgrounds/bg_sky.png";
        }
    }

    private String musicPathForWorld() {
        switch (worldId) {
            case Constants.WORLD_CAVE:  return "sounds/music/music_gameplay_alt.ogg";
            case Constants.WORLD_SPACE: return "sounds/music/music_gameplay_space.ogg";
            default:                    return "sounds/music/music_gameplay.ogg";
        }
    }
}

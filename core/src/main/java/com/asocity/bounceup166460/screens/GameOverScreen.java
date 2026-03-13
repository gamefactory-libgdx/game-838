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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameOverScreen implements Screen {

    private final MainGame game;
    private final int score;     // height reached (metres)
    private final int stars;     // stars collected this run
    private final int worldId;   // which world was played (Constants.WORLD_SKY/CAVE/SPACE)

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage stage;

    private static final String[] ASSETS = {
        "ui/game_over.png",
        "sprites/button_blue.png",
        "sprites/button_blue_pressed.png",
        "sprites/button_grey.png",
        "sprites/button_grey_pressed.png",
        "sprites/icon_star.png",
        "sounds/music/music_game_over.ogg",
        "sounds/sfx/sfx_button_click.ogg",
        "sounds/sfx/sfx_game_over.ogg"
    };

    /** Convenience constructor for callers that don't specify a world (defaults to Sky). */
    public GameOverScreen(MainGame game, int score, int extra) {
        this(game, score, extra, Constants.WORLD_SKY);
    }

    /**
     * @param game    the main game instance
     * @param score   height reached in metres
     * @param extra   stars collected during the run
     * @param worldId which world was played (Constants.WORLD_SKY/CAVE/SPACE)
     */
    public GameOverScreen(MainGame game, int score, int extra, int worldId) {
        this.game    = game;
        this.score   = score;
        this.stars   = extra;
        this.worldId = worldId;

        for (String path : ASSETS) {
            if (!game.manager.isLoaded(path)) {
                if (path.endsWith(".ogg") && path.contains("music"))
                    game.manager.load(path, Music.class);
                else if (path.endsWith(".ogg"))
                    game.manager.load(path, Sound.class);
                else
                    game.manager.load(path, Texture.class);
            }
        }
        game.manager.finishLoading();

        // Persist stars earned, update per-world best height, and save leaderboard
        persistResults();

        camera   = new OrthographicCamera();
        viewport = new StretchViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        stage    = new Stage(viewport, game.batch);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    game.setScreen(new MainMenuScreen(game));
                    return true;
                }
                return false;
            }
        }));

        buildUI();

        game.playMusicOnce("sounds/music/music_game_over.ogg");
        if (game.sfxEnabled)
            game.manager.get("sounds/sfx/sfx_game_over.ogg", Sound.class).play(1.0f);
    }

    private void persistResults() {
        Preferences prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);

        // Add stars to total
        int totalStars = prefs.getInteger(Constants.PREF_STARS, 0) + stars;
        prefs.putInteger(Constants.PREF_STARS, totalStars);

        // Update per-world best height
        String bestKey = getBestHeightKey();
        int prevBest = prefs.getInteger(bestKey, 0);
        if (score > prevBest) prefs.putInteger(bestKey, score);

        prefs.flush();

        // Add to per-world leaderboard
        LeaderboardScreen.addScore(score, worldId);
    }

    private String getBestHeightKey() {
        switch (worldId) {
            case Constants.WORLD_CAVE:  return Constants.PREF_BEST_HEIGHT_CAVE;
            case Constants.WORLD_SPACE: return Constants.PREF_BEST_HEIGHT_SPACE;
            default:                    return Constants.PREF_BEST_HEIGHT_SKY;
        }
    }

    private TextButton.TextButtonStyle makeStyle(String up, String down) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font      = game.fontBody;
        s.up        = new TextureRegionDrawable(game.manager.get(up,   Texture.class));
        s.down      = new TextureRegionDrawable(game.manager.get(down, Texture.class));
        s.fontColor = Color.WHITE;
        return s;
    }

    private void buildUI() {
        Image bg = new Image(game.manager.get("ui/game_over.png", Texture.class));
        bg.setSize(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        stage.addActor(bg);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label.LabelStyle headStyle  = new Label.LabelStyle(game.fontTitle, new Color(1f, 0.42f, 0.42f, 1f)); // #FF6B6B
        Label.LabelStyle scoreStyle = new Label.LabelStyle(game.fontScore, Color.WHITE);
        Label.LabelStyle bodyStyle  = new Label.LabelStyle(game.fontBody,  Color.WHITE);
        Label.LabelStyle smallStyle = new Label.LabelStyle(game.fontSmall, new Color(0.8f, 0.8f, 0.8f, 1f));

        root.add(new Label("GAME OVER", headStyle)).padTop(80).padBottom(30).row();

        // Height score
        root.add(new Label(score + " m", scoreStyle)).padBottom(10).row();

        // Stars collected
        root.add(new Label("STARS: " + stars, bodyStyle)).padBottom(16).row();

        // Personal best for this world
        int bestHeight = Gdx.app.getPreferences(Constants.PREFS_NAME).getInteger(getBestHeightKey(), 0);
        String worldLabel;
        switch (worldId) {
            case Constants.WORLD_CAVE:  worldLabel = "CAVE"; break;
            case Constants.WORLD_SPACE: worldLabel = "SPACE"; break;
            default:                    worldLabel = "SKY"; break;
        }
        root.add(new Label("BEST (" + worldLabel + "): " + bestHeight + " m", smallStyle))
            .padBottom(50).row();

        // Buttons row
        Table btnRow = new Table();

        TextButton retryBtn = new TextButton("RETRY",
            makeStyle("sprites/button_blue.png", "sprites/button_blue_pressed.png"));
        retryBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.sfxEnabled)
                    game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class).play(1.0f);
                // Create a fresh game screen for the same world
                game.setScreen(new WorldSelectScreen(game));
            }
        });
        btnRow.add(retryBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT).padRight(20);

        TextButton menuBtn = new TextButton("MAIN MENU",
            makeStyle("sprites/button_grey.png", "sprites/button_grey_pressed.png"));
        menuBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.sfxEnabled)
                    game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class).play(1.0f);
                game.setScreen(new MainMenuScreen(game));
            }
        });
        btnRow.add(menuBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT);

        root.add(btnRow).row();
    }

    @Override public void show()   {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        for (String path : ASSETS) {
            if (game.manager.isLoaded(path)) game.manager.unload(path);
        }
    }
}

package com.asocity.bounceup166460.screens;

import com.asocity.bounceup166460.Constants;
import com.asocity.bounceup166460.MainGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
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

public class LeaderboardScreen implements Screen {

    private final MainGame game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage stage;

    private int activeTab = Constants.WORLD_SKY;

    private static final String[] ASSETS = {
        "ui/leaderboard.png",
        "sprites/button_grey.png",
        "sprites/button_grey_pressed.png",
        "sprites/button_blue.png",
        "sprites/button_blue_pressed.png",
        "sprites/icon_trophy.png",
        "sounds/sfx/sfx_button_click.ogg"
    };

    // -------------------------------------------------------------------------
    // Static helpers — called by GameOverScreen to persist scores
    // -------------------------------------------------------------------------

    /** Add score to the Sky (default) world leaderboard. */
    public static void addScore(int score) {
        addScore(score, Constants.WORLD_SKY);
    }

    /**
     * Insert {@code score} into the top-10 leaderboard for {@code worldId},
     * maintaining descending order.
     */
    public static void addScore(int score, int worldId) {
        Preferences prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);
        String prefix = prefixFor(worldId);

        // Read current scores
        int[] scores = new int[Constants.LEADERBOARD_SIZE];
        for (int i = 0; i < Constants.LEADERBOARD_SIZE; i++) {
            scores[i] = prefs.getInteger(prefix + i, 0);
        }

        // Insert-sort descending
        for (int i = 0; i < Constants.LEADERBOARD_SIZE; i++) {
            if (score > scores[i]) {
                // Shift lower scores down
                for (int j = Constants.LEADERBOARD_SIZE - 1; j > i; j--) {
                    scores[j] = scores[j - 1];
                }
                scores[i] = score;
                break;
            }
        }

        // Write back
        for (int i = 0; i < Constants.LEADERBOARD_SIZE; i++) {
            prefs.putInteger(prefix + i, scores[i]);
        }
        prefs.flush();
    }

    private static String prefixFor(int worldId) {
        switch (worldId) {
            case Constants.WORLD_CAVE:  return Constants.PREF_LEADERBOARD_CAVE_PREFIX;
            case Constants.WORLD_SPACE: return Constants.PREF_LEADERBOARD_SPACE_PREFIX;
            default:                    return Constants.PREF_LEADERBOARD_SKY_PREFIX;
        }
    }

    // -------------------------------------------------------------------------
    // Screen implementation
    // -------------------------------------------------------------------------

    public LeaderboardScreen(MainGame game) {
        this.game = game;

        for (String path : ASSETS) {
            if (!game.manager.isLoaded(path)) {
                if (path.endsWith(".ogg")) game.manager.load(path, Sound.class);
                else                        game.manager.load(path, Texture.class);
            }
        }
        game.manager.finishLoading();

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
        game.playMusic("sounds/music/music_menu.ogg");
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
        stage.clear();

        Image bg = new Image(game.manager.get("ui/leaderboard.png", Texture.class));
        bg.setSize(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        stage.addActor(bg);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label.LabelStyle headStyle  = new Label.LabelStyle(game.fontTitle, Color.WHITE);
        Label.LabelStyle bodyStyle  = new Label.LabelStyle(game.fontBody,  Color.WHITE);
        Label.LabelStyle rankStyle  = new Label.LabelStyle(game.fontSmall, Color.WHITE);
        Label.LabelStyle scoreStyle = new Label.LabelStyle(game.fontSmall, new Color(1f, 0.85f, 0f, 1f)); // gold

        root.add(new Label("LEADERBOARD", headStyle)).padTop(50).padBottom(20).colspan(3).row();

        // --- Tab bar ---
        Table tabs = new Table();
        String[] tabLabels = { "SKY", "CAVE", "SPACE" };
        int[] tabWorlds    = { Constants.WORLD_SKY, Constants.WORLD_CAVE, Constants.WORLD_SPACE };

        for (int i = 0; i < 3; i++) {
            final int worldId = tabWorlds[i];
            String upFile   = (activeTab == worldId) ? "sprites/button_blue.png"         : "sprites/button_grey.png";
            String downFile = (activeTab == worldId) ? "sprites/button_blue_pressed.png" : "sprites/button_grey_pressed.png";
            TextButton tabBtn = new TextButton(tabLabels[i], makeStyle(upFile, downFile));
            tabBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    activeTab = worldId;
                    buildUI();
                }
            });
            tabs.add(tabBtn).size(130, 55).padRight(i < 2 ? 8 : 0);
        }
        root.add(tabs).colspan(3).padBottom(20).row();

        // --- Score list ---
        Preferences prefs  = Gdx.app.getPreferences(Constants.PREFS_NAME);
        String prefix      = prefixFor(activeTab);
        Color rowOdd       = new Color(1f,  1f,  1f,  0.15f);
        Color rowEven      = new Color(0.9f, 0.9f, 0.9f, 0.08f);

        for (int i = 0; i < Constants.LEADERBOARD_SIZE; i++) {
            int s = prefs.getInteger(prefix + i, 0);

            Table row = new Table();
            row.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable() {
                // lightweight background — just colour the row via a filled rect
            });

            Label rankLabel  = new Label((i + 1) + ".", rankStyle);
            Label scoreLabel = new Label(s > 0 ? (s + " m") : "---", scoreStyle);

            root.add(rankLabel).width(50).left().padLeft(20);
            root.add(new Label("", rankStyle)).width(200); // spacer
            root.add(scoreLabel).width(130).right().padRight(20);
            root.row().padBottom(4);
        }

        root.add(new Label("", bodyStyle)).padTop(20).colspan(3).row(); // spacer

        // --- Main Menu button ---
        TextButton menuBtn = new TextButton("MAIN MENU",
            makeStyle("sprites/button_grey.png", "sprites/button_grey_pressed.png"));
        menuBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.sfxEnabled)
                    game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class).play(1.0f);
                game.setScreen(new MainMenuScreen(game));
            }
        });
        root.add(menuBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT)
            .colspan(3).padBottom(30).row();
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

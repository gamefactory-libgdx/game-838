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

public class MainMenuScreen implements Screen {

    private final MainGame game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage stage;

    private static final String[] ASSETS = {
        "ui/main_menu.png",
        "sprites/button_blue.png",
        "sprites/button_blue_pressed.png",
        "sprites/button_grey.png",
        "sprites/button_grey_pressed.png",
        "sounds/music/music_menu.ogg"
    };

    public MainMenuScreen(MainGame game) {
        this.game = game;

        for (String path : ASSETS) {
            if (!game.manager.isLoaded(path)) {
                if (path.endsWith(".ogg")) game.manager.load(path, Music.class);
                else                        game.manager.load(path, Texture.class);
            }
        }
        game.manager.finishLoading();

        // Apply saved settings
        Preferences prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);
        game.musicEnabled = prefs.getBoolean(Constants.PREF_MUSIC, true);
        game.sfxEnabled   = prefs.getBoolean(Constants.PREF_SFX, true);

        camera   = new OrthographicCamera();
        viewport = new StretchViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        stage    = new Stage(viewport, game.batch);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK) return true; // main menu — no back destination
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
        Image bg = new Image(game.manager.get("ui/main_menu.png", Texture.class));
        bg.setSize(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        stage.addActor(bg);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Title
        Label.LabelStyle titleStyle = new Label.LabelStyle(game.fontTitle, Color.WHITE);
        root.add(new Label("BOUNCE UP", titleStyle)).padTop(80).padBottom(60).row();

        // Play
        TextButton playBtn = new TextButton("PLAY",
            makeStyle("sprites/button_blue.png", "sprites/button_blue_pressed.png"));
        playBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new WorldSelectScreen(game));
            }
        });
        root.add(playBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT).padBottom(24).row();

        // Shop
        TextButton shopBtn = new TextButton("SHOP",
            makeStyle("sprites/button_grey.png", "sprites/button_grey_pressed.png"));
        shopBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new ShopScreen(game));
            }
        });
        root.add(shopBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT).padBottom(16).row();

        // Leaderboard
        TextButton lbBtn = new TextButton("LEADERBOARD",
            makeStyle("sprites/button_grey.png", "sprites/button_grey_pressed.png"));
        lbBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LeaderboardScreen(game));
            }
        });
        root.add(lbBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT).padBottom(16).row();

        // Settings
        TextButton settingsBtn = new TextButton("SETTINGS",
            makeStyle("sprites/button_grey.png", "sprites/button_grey_pressed.png"));
        settingsBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SettingsScreen(game));
            }
        });
        root.add(settingsBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT).row();
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

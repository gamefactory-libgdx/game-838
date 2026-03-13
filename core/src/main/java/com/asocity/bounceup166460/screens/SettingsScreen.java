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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SettingsScreen implements Screen {

    private final MainGame game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage stage;

    private final Preferences prefs;

    private static final String[] ASSETS = {
        "ui/settings.png",
        "sprites/button_grey.png",
        "sprites/button_grey_pressed.png",
        "sprites/button_blue.png",
        "sprites/button_blue_pressed.png",
        "sprites/icon_music_on.png",
        "sprites/icon_music_off.png",
        "sprites/icon_sfx_on.png",
        "sprites/icon_sfx_off.png",
        "sounds/sfx/sfx_toggle.ogg",
        "sounds/sfx/sfx_button_click.ogg"
    };

    public SettingsScreen(MainGame game) {
        this.game  = game;
        this.prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);

        for (String path : ASSETS) {
            if (!game.manager.isLoaded(path)) {
                if (path.endsWith(".ogg")) game.manager.load(path, Sound.class);
                else                        game.manager.load(path, Texture.class);
            }
        }
        game.manager.finishLoading();

        game.musicEnabled = prefs.getBoolean(Constants.PREF_MUSIC, true);
        game.sfxEnabled   = prefs.getBoolean(Constants.PREF_SFX, true);

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
    }

    private TextButton.TextButtonStyle makeStyle(String up, String down) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font      = game.fontBody;
        s.up        = new TextureRegionDrawable(game.manager.get(up,   Texture.class));
        s.down      = new TextureRegionDrawable(game.manager.get(down, Texture.class));
        s.fontColor = Color.WHITE;
        return s;
    }

    private void playToggleSfx() {
        if (game.sfxEnabled) game.manager.get("sounds/sfx/sfx_toggle.ogg", Sound.class).play(0.5f);
    }

    private void buildUI() {
        Image bg = new Image(game.manager.get("ui/settings.png", Texture.class));
        bg.setSize(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        stage.addActor(bg);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label.LabelStyle headStyle = new Label.LabelStyle(game.fontTitle, Color.WHITE);
        Label.LabelStyle bodyStyle = new Label.LabelStyle(game.fontBody,  Color.WHITE);

        root.add(new Label("SETTINGS", headStyle)).padTop(60).padBottom(50).colspan(2).row();

        // --- Music toggle ---
        root.add(new Label("MUSIC", bodyStyle)).left().padBottom(20).padRight(20);
        final ImageButton musicBtn = new ImageButton(
            new TextureRegionDrawable(game.manager.get("sprites/icon_music_on.png",  Texture.class)),
            new TextureRegionDrawable(game.manager.get("sprites/icon_music_off.png", Texture.class))
        );
        musicBtn.setChecked(!game.musicEnabled);
        musicBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.musicEnabled = !musicBtn.isChecked();
                prefs.putBoolean(Constants.PREF_MUSIC, game.musicEnabled);
                prefs.flush();
                if (game.currentMusic != null) {
                    if (game.musicEnabled) game.currentMusic.play();
                    else                   game.currentMusic.pause();
                }
                playToggleSfx();
            }
        });
        root.add(musicBtn).size(Constants.ICON_BUTTON_SIZE, Constants.ICON_BUTTON_SIZE)
            .padBottom(20).row();

        // --- SFX toggle ---
        root.add(new Label("SOUND FX", bodyStyle)).left().padBottom(20).padRight(20);
        final ImageButton sfxBtn = new ImageButton(
            new TextureRegionDrawable(game.manager.get("sprites/icon_sfx_on.png",  Texture.class)),
            new TextureRegionDrawable(game.manager.get("sprites/icon_sfx_off.png", Texture.class))
        );
        sfxBtn.setChecked(!game.sfxEnabled);
        sfxBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.sfxEnabled = !sfxBtn.isChecked();
                prefs.putBoolean(Constants.PREF_SFX, game.sfxEnabled);
                prefs.flush();
                playToggleSfx();
            }
        });
        root.add(sfxBtn).size(Constants.ICON_BUTTON_SIZE, Constants.ICON_BUTTON_SIZE)
            .padBottom(30).row();

        // --- Difficulty ---
        root.add(new Label("DIFFICULTY", bodyStyle)).left().padBottom(12).colspan(2).row();

        int savedDiff = prefs.getInteger(Constants.PREF_DIFFICULTY, Constants.DIFFICULTY_NORMAL);
        String[] diffLabels  = { "EASY", "NORMAL", "HARD" };
        String[] upStyles    = { "sprites/button_grey.png", "sprites/button_blue.png",  "sprites/button_grey.png" };
        String[] downStyles  = { "sprites/button_grey_pressed.png", "sprites/button_blue_pressed.png", "sprites/button_grey_pressed.png" };

        Table diffRow = new Table();
        for (int i = 0; i < 3; i++) {
            final int diffLevel = i;
            String upFile   = savedDiff == i ? "sprites/button_blue.png"         : "sprites/button_grey.png";
            String downFile = savedDiff == i ? "sprites/button_blue_pressed.png" : "sprites/button_grey_pressed.png";
            TextButton btn = new TextButton(diffLabels[i], makeStyle(upFile, downFile));
            btn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    prefs.putInteger(Constants.PREF_DIFFICULTY, diffLevel);
                    prefs.flush();
                    playToggleSfx();
                    // Rebuild to update button highlight
                    stage.clear();
                    buildUI();
                }
            });
            diffRow.add(btn).size(130, 55).padRight(i < 2 ? 8 : 0);
        }
        root.add(diffRow).colspan(2).padBottom(50).row();

        // --- Main Menu ---
        TextButton menuBtn = new TextButton("MAIN MENU",
            makeStyle("sprites/button_grey.png", "sprites/button_grey_pressed.png"));
        menuBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.sfxEnabled)
                    game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class).play(1.0f);
                game.setScreen(new MainMenuScreen(game));
            }
        });
        root.add(menuBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT).colspan(2).row();
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

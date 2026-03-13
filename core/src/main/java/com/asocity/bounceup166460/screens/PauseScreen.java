package com.asocity.bounceup166460.screens;

import com.asocity.bounceup166460.Constants;
import com.asocity.bounceup166460.MainGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Pause overlay shown when the player taps the pause button during gameplay.
 * Keeps the GameScreen instance alive so it can be resumed.
 */
public class PauseScreen implements Screen {

    private final MainGame game;
    private final GameScreen gameScreen; // kept alive for resume
    private final int worldId;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage stage;

    private static final String[] ASSETS = {
        "sprites/button_blue.png",
        "sprites/button_blue_pressed.png",
        "sprites/button_red.png",
        "sprites/button_red_pressed.png",
        "sprites/button_grey.png",
        "sprites/button_grey_pressed.png",
        "sounds/sfx/sfx_button_click.ogg"
    };

    public PauseScreen(MainGame game, GameScreen gameScreen, int worldId) {
        this.game       = game;
        this.gameScreen = gameScreen;
        this.worldId    = worldId;

        for (String path : ASSETS) {
            if (!game.manager.isLoaded(path)) {
                if (path.endsWith(".ogg"))
                    game.manager.load(path, Sound.class);
                else
                    game.manager.load(path, Texture.class);
            }
        }
        game.manager.finishLoading();

        // Pause music while paused
        if (game.currentMusic != null && game.currentMusic.isPlaying()) {
            game.currentMusic.pause();
        }

        camera   = new OrthographicCamera();
        viewport = new StretchViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        stage    = new Stage(viewport, game.batch);

        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    resumeGame();
                    return true;
                }
                return false;
            }
        }));

        buildUI();
    }

    private void resumeGame() {
        // Resume music
        if (game.currentMusic != null && game.musicEnabled) {
            game.currentMusic.play();
        }
        game.setScreen(gameScreen);
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
        Label.LabelStyle titleStyle = new Label.LabelStyle(game.fontTitle, Color.WHITE);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        root.add(new Label("PAUSED", titleStyle)).padBottom(60).row();

        // Resume
        TextButton resumeBtn = new TextButton("RESUME",
            makeStyle("sprites/button_blue.png", "sprites/button_blue_pressed.png"));
        resumeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.sfxEnabled)
                    game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class).play(1.0f);
                resumeGame();
            }
        });
        root.add(resumeBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT).padBottom(20).row();

        // Restart (new GameScreen instance)
        TextButton restartBtn = new TextButton("RESTART",
            makeStyle("sprites/button_red.png", "sprites/button_red_pressed.png"));
        final int wid = worldId;
        restartBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.sfxEnabled)
                    game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class).play(1.0f);
                game.setScreen(new GameScreen(game, wid));
            }
        });
        root.add(restartBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT).padBottom(20).row();

        // Main Menu
        TextButton menuBtn = new TextButton("MAIN MENU",
            makeStyle("sprites/button_grey.png", "sprites/button_grey_pressed.png"));
        menuBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.sfxEnabled)
                    game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class).play(1.0f);
                game.setScreen(new MainMenuScreen(game));
            }
        });
        root.add(menuBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT).row();
    }

    @Override public void show()   {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void render(float delta) {
        // Dark semi-transparent overlay
        ScreenUtils.clear(0.05f, 0.05f, 0.15f, 0.92f);
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
        // Do not unload assets shared with GameScreen — GameScreen disposes them.
    }
}

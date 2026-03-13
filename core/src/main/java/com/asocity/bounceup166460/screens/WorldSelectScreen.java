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

public class WorldSelectScreen implements Screen {

    private final MainGame game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage stage;

    private static final String[] ASSETS = {
        "ui/world_select.png",
        "sprites/button_blue.png",
        "sprites/button_blue_pressed.png",
        "sprites/button_grey.png",
        "sprites/button_grey_pressed.png",
        "sprites/button_green.png",
        "sprites/button_green_pressed.png",
        "sprites/icon_locked.png",
        "sprites/icon_star.png",
        "sounds/music/music_menu.ogg",
        "sounds/sfx/sfx_button_click.ogg",
        "sounds/sfx/sfx_select.ogg",
        "sounds/sfx/sfx_error.ogg"
    };

    public WorldSelectScreen(MainGame game) {
        this.game = game;

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

        Image bg = new Image(game.manager.get("ui/world_select.png", Texture.class));
        bg.setSize(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        stage.addActor(bg);

        Preferences prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);
        boolean caveUnlocked  = prefs.getBoolean(Constants.PREF_WORLD_CAVE_UNLOCKED,  false);
        boolean spaceUnlocked = prefs.getBoolean(Constants.PREF_WORLD_SPACE_UNLOCKED, false);
        int totalStars        = prefs.getInteger(Constants.PREF_STARS, 0);
        int bestSky           = prefs.getInteger(Constants.PREF_BEST_HEIGHT_SKY,   0);
        int bestCave          = prefs.getInteger(Constants.PREF_BEST_HEIGHT_CAVE,  0);
        int bestSpace         = prefs.getInteger(Constants.PREF_BEST_HEIGHT_SPACE, 0);

        Label.LabelStyle headStyle  = new Label.LabelStyle(game.fontTitle, Color.WHITE);
        Label.LabelStyle bodyStyle  = new Label.LabelStyle(game.fontBody,  Color.WHITE);
        Label.LabelStyle smallStyle = new Label.LabelStyle(game.fontSmall, new Color(0.9f, 0.9f, 0.9f, 1f));
        Label.LabelStyle goldStyle  = new Label.LabelStyle(game.fontSmall, new Color(1f, 0.85f, 0f, 1f));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        root.add(new Label("SELECT WORLD", headStyle)).padTop(60).padBottom(16).row();

        // Stars balance row
        Table balRow = new Table();
        balRow.add(new Image(game.manager.get("sprites/icon_star.png", Texture.class))).size(28, 28).padRight(8);
        balRow.add(new Label(totalStars + " STARS", goldStyle));
        root.add(balRow).padBottom(30).row();

        // World cards
        addWorldCard(root, "SKY",   "✓ UNLOCKED", bestSky,   Constants.WORLD_SKY,   true,
                     bodyStyle, smallStyle, goldStyle);
        addWorldCard(root, "CAVE",  caveUnlocked ? "✓ UNLOCKED" : "UNLOCK: " + Constants.WORLD_CAVE_UNLOCK_COST + " ★",
                     bestCave,  Constants.WORLD_CAVE,  caveUnlocked, bodyStyle, smallStyle, goldStyle);
        addWorldCard(root, "SPACE", spaceUnlocked ? "✓ UNLOCKED" : "UNLOCK: " + Constants.WORLD_SPACE_UNLOCK_COST + " ★",
                     bestSpace, Constants.WORLD_SPACE, spaceUnlocked, bodyStyle, smallStyle, goldStyle);

        root.add(new Label("", smallStyle)).expandY().row(); // push up

        // Back button
        TextButton backBtn = new TextButton("MAIN MENU",
            makeStyle("sprites/button_grey.png", "sprites/button_grey_pressed.png"));
        backBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.sfxEnabled)
                    game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class).play(1.0f);
                game.setScreen(new MainMenuScreen(game));
            }
        });
        root.add(backBtn).size(Constants.BUTTON_SECONDARY_WIDTH, Constants.BUTTON_SECONDARY_HEIGHT).padBottom(40).row();
    }

    private void addWorldCard(Table root, String name, String statusText, int bestHeight,
                               int worldId, boolean unlocked,
                               Label.LabelStyle bodyStyle, Label.LabelStyle smallStyle,
                               Label.LabelStyle goldStyle) {
        Table card = new Table();
        card.pad(16);

        // World name + status
        Table titleRow = new Table();
        titleRow.add(new Label(name, bodyStyle)).expandX().left();
        titleRow.add(new Label(statusText, unlocked ? goldStyle : smallStyle)).right();
        card.add(titleRow).fillX().row();

        // Best height
        card.add(new Label("BEST: " + (bestHeight > 0 ? bestHeight + " m" : "---"), smallStyle))
            .padTop(6).left().row();

        // Action button
        final int wid = worldId;
        final boolean isUnlocked = unlocked;
        String upFile   = unlocked ? "sprites/button_blue.png"  : "sprites/button_green.png";
        String downFile = unlocked ? "sprites/button_blue_pressed.png" : "sprites/button_green_pressed.png";
        String btnText  = unlocked ? "PLAY" : "UNLOCK";

        TextButton actionBtn = new TextButton(btnText, makeStyle(upFile, downFile));
        actionBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (isUnlocked) {
                    if (game.sfxEnabled)
                        game.manager.get("sounds/sfx/sfx_select.ogg", Sound.class).play(1.0f);
                    game.setScreen(new GameScreen(game, wid));
                } else {
                    tryUnlock(wid);
                }
            }
        });
        card.add(actionBtn).size(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT).padTop(10).row();

        root.add(card).width(Constants.WORLD_CARD_WIDTH).height(Constants.WORLD_CARD_HEIGHT + 60)
            .padBottom(16).row();
    }

    private void tryUnlock(int worldId) {
        Preferences prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);
        int totalStars = prefs.getInteger(Constants.PREF_STARS, 0);
        int cost = (worldId == Constants.WORLD_CAVE)
            ? Constants.WORLD_CAVE_UNLOCK_COST
            : Constants.WORLD_SPACE_UNLOCK_COST;

        if (totalStars >= cost) {
            prefs.putInteger(Constants.PREF_STARS, totalStars - cost);
            if (worldId == Constants.WORLD_CAVE)
                prefs.putBoolean(Constants.PREF_WORLD_CAVE_UNLOCKED, true);
            else
                prefs.putBoolean(Constants.PREF_WORLD_SPACE_UNLOCKED, true);
            prefs.flush();
            if (game.sfxEnabled)
                game.manager.get("sounds/sfx/sfx_select.ogg", Sound.class).play(1.0f);
            buildUI(); // refresh
        } else {
            if (game.sfxEnabled)
                game.manager.get("sounds/sfx/sfx_error.ogg", Sound.class).play(1.0f);
        }
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

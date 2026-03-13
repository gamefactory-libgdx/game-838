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

/**
 * Shop screen — two sections:
 *   1. Power-ups (consumable, one-use per run)
 *   2. Character skins (permanent unlock)
 * All purchases use stars collected during gameplay.
 */
public class ShopScreen implements Screen {

    private final MainGame game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage stage;

    private static final String[] ASSETS = {
        "ui/shop.png",
        "sprites/button_blue.png",
        "sprites/button_blue_pressed.png",
        "sprites/button_green.png",
        "sprites/button_green_pressed.png",
        "sprites/button_yellow.png",
        "sprites/button_yellow_pressed.png",
        "sprites/button_grey.png",
        "sprites/button_grey_pressed.png",
        "sprites/icon_star.png",
        "sprites/jumper/player_stand.png",
        "sprites/jumper/player2_stand.png",
        "sounds/music/music_menu.ogg",
        "sounds/sfx/sfx_button_click.ogg",
        "sounds/sfx/sfx_coin.ogg",
        "sounds/sfx/sfx_error.ogg",
        "sounds/sfx/sfx_power_up.ogg"
    };

    public ShopScreen(MainGame game) {
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

        Image bg = new Image(game.manager.get("ui/shop.png", Texture.class));
        bg.setSize(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        stage.addActor(bg);

        Preferences prefs    = Gdx.app.getPreferences(Constants.PREFS_NAME);
        final int totalStars = prefs.getInteger(Constants.PREF_STARS, 0);
        int selectedSkin     = prefs.getInteger(Constants.PREF_SKIN, 0);
        boolean skin2Owned   = prefs.getBoolean(Constants.PREF_SKIN_2_UNLOCKED, false);
        boolean skin3Owned   = prefs.getBoolean(Constants.PREF_SKIN_3_UNLOCKED, false);
        boolean shieldQueued = prefs.getBoolean(Constants.PREF_POWERUP_SHIELD, false);
        boolean magnetQueued = prefs.getBoolean(Constants.PREF_POWERUP_MAGNET, false);
        boolean doubleQueued = prefs.getBoolean(Constants.PREF_POWERUP_DOUBLE, false);

        Label.LabelStyle headStyle  = new Label.LabelStyle(game.fontTitle, Color.WHITE);
        Label.LabelStyle bodyStyle  = new Label.LabelStyle(game.fontBody,  Color.WHITE);
        Label.LabelStyle smallStyle = new Label.LabelStyle(game.fontSmall, new Color(0.9f, 0.9f, 0.9f, 1f));
        Label.LabelStyle goldStyle  = new Label.LabelStyle(game.fontHud,   new Color(1f, 0.85f, 0f, 1f));

        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(20);
        stage.addActor(root);

        // Title
        root.add(new Label("SHOP", headStyle)).padBottom(8).colspan(2).row();

        // Star balance
        Table balRow = new Table();
        balRow.add(new Image(game.manager.get("sprites/icon_star.png", Texture.class))).size(28, 28).padRight(6);
        balRow.add(new Label(totalStars + " STARS", goldStyle));
        root.add(balRow).padBottom(20).colspan(2).row();

        // ── Section 1: Power-ups ──
        root.add(new Label("POWER-UPS", bodyStyle)).padBottom(10).colspan(2).left().row();

        addPowerUpRow(root, "Shield",     "5s invincibility",  Constants.POWERUP_SHIELD_PRICE,
                      shieldQueued, Constants.PREF_POWERUP_SHIELD, totalStars, bodyStyle, smallStyle);
        addPowerUpRow(root, "Star Magnet","Auto-collects stars",Constants.POWERUP_MAGNET_PRICE,
                      magnetQueued, Constants.PREF_POWERUP_MAGNET, totalStars, bodyStyle, smallStyle);
        addPowerUpRow(root, "2x Score",   "Double stars earned",Constants.POWERUP_DOUBLE_PRICE,
                      doubleQueued, Constants.PREF_POWERUP_DOUBLE, totalStars, bodyStyle, smallStyle);

        root.add(new Label("", smallStyle)).height(16).colspan(2).row(); // spacer

        // ── Section 2: Character Skins ──
        root.add(new Label("CHARACTER SKINS", bodyStyle)).padBottom(10).colspan(2).left().row();

        addSkinRow(root, "Blue Alien",  "sprites/jumper/player_stand.png",  0, Constants.SKIN_1_PRICE,
                   true,   (selectedSkin == 0), totalStars, bodyStyle, smallStyle, goldStyle);
        addSkinRow(root, "Green Alien", "sprites/jumper/player2_stand.png", 1, Constants.SKIN_2_PRICE,
                   skin2Owned, (selectedSkin == 1), totalStars, bodyStyle, smallStyle, goldStyle);
        addSkinRow(root, "Pink Alien",  "sprites/jumper/player_stand.png",  2, Constants.SKIN_3_PRICE,
                   skin3Owned, (selectedSkin == 2), totalStars, bodyStyle, smallStyle, goldStyle);

        root.add(new Label("", smallStyle)).expandY().row(); // push down

        // Back / Main Menu
        TextButton backBtn = new TextButton("MAIN MENU",
            makeStyle("sprites/button_grey.png", "sprites/button_grey_pressed.png"));
        backBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.sfxEnabled)
                    game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class).play(1.0f);
                game.setScreen(new MainMenuScreen(game));
            }
        });
        root.add(backBtn).size(Constants.BUTTON_SECONDARY_WIDTH, Constants.BUTTON_SECONDARY_HEIGHT)
            .colspan(2).padBottom(24).row();
    }

    /** Adds a single power-up row: name, description, cost, BUY/READY button. */
    private void addPowerUpRow(Table root, String name, String desc, int cost,
                                boolean queued, final String prefKey,
                                int playerStars,
                                Label.LabelStyle bodyStyle, Label.LabelStyle smallStyle) {
        Table row = new Table();
        row.pad(6, 12, 6, 12);

        Table info = new Table();
        info.add(new Label(name, bodyStyle)).left().row();
        info.add(new Label(desc + " | " + cost + " ★", smallStyle)).left().row();
        row.add(info).expandX().left();

        if (queued) {
            TextButton readyBtn = new TextButton("READY",
                makeStyle("sprites/button_green.png", "sprites/button_green_pressed.png"));
            readyBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    // Cancel queue
                    if (game.sfxEnabled)
                        game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class).play(0.8f);
                    Preferences p = Gdx.app.getPreferences(Constants.PREFS_NAME);
                    p.putBoolean(prefKey, false);
                    p.flush();
                    buildUI();
                }
            });
            row.add(readyBtn).size(120, 50);
        } else {
            TextButton buyBtn = new TextButton("BUY " + cost + "★",
                makeStyle("sprites/button_yellow.png", "sprites/button_yellow_pressed.png"));
            buyBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    purchasePowerUp(prefKey, cost);
                }
            });
            row.add(buyBtn).size(120, 50);
        }

        root.add(row).fillX().padBottom(4).colspan(2).row();
    }

    private void purchasePowerUp(String prefKey, int cost) {
        Preferences prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);
        int stars = prefs.getInteger(Constants.PREF_STARS, 0);
        if (prefs.getBoolean(prefKey, false)) return; // already queued
        if (stars < cost) {
            if (game.sfxEnabled)
                game.manager.get("sounds/sfx/sfx_error.ogg", Sound.class).play(1.0f);
            return;
        }
        prefs.putInteger(Constants.PREF_STARS, stars - cost);
        prefs.putBoolean(prefKey, true);
        prefs.flush();
        if (game.sfxEnabled)
            game.manager.get("sounds/sfx/sfx_power_up.ogg", Sound.class).play(1.0f);
        buildUI();
    }

    /**
     * Adds a skin card row: preview sprite, name, cost, BUY/OWNED/EQUIP button.
     * skinIndex 2 (Pink Alien) reuses the default player sprite with a pink tint applied in GameScreen.
     */
    private void addSkinRow(Table root, String skinName, String spritePath,
                             final int skinIndex, int price,
                             boolean owned, boolean equipped,
                             int playerStars,
                             Label.LabelStyle bodyStyle, Label.LabelStyle smallStyle,
                             Label.LabelStyle goldStyle) {
        Table row = new Table();
        row.pad(6, 12, 6, 12);

        // Preview sprite (pink tint for skin 2)
        Image preview = new Image(game.manager.get(spritePath, Texture.class));
        if (skinIndex == 2) preview.setColor(new Color(1f, 0.6f, 0.8f, 1f));
        row.add(preview).size(56, 64).padRight(12);

        // Info
        Table info = new Table();
        String priceLabel = price == 0 ? "FREE" : price + " ★";
        info.add(new Label(skinName, bodyStyle)).left().row();
        info.add(new Label(priceLabel, goldStyle)).left().row();
        row.add(info).expandX().left();

        // Action button
        if (equipped) {
            TextButton equippedBtn = new TextButton("EQUIPPED",
                makeStyle("sprites/button_blue.png", "sprites/button_blue_pressed.png"));
            equippedBtn.setDisabled(true);
            row.add(equippedBtn).size(130, 50);
        } else if (owned || price == 0) {
            TextButton equipBtn = new TextButton("EQUIP",
                makeStyle("sprites/button_green.png", "sprites/button_green_pressed.png"));
            equipBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    if (game.sfxEnabled)
                        game.manager.get("sounds/sfx/sfx_button_click.ogg", Sound.class).play(1.0f);
                    Preferences p = Gdx.app.getPreferences(Constants.PREFS_NAME);
                    p.putInteger(Constants.PREF_SKIN, skinIndex);
                    p.flush();
                    buildUI();
                }
            });
            row.add(equipBtn).size(130, 50);
        } else {
            TextButton buyBtn = new TextButton("BUY " + price + "★",
                makeStyle("sprites/button_yellow.png", "sprites/button_yellow_pressed.png"));
            buyBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    purchaseSkin(skinIndex, price);
                }
            });
            row.add(buyBtn).size(130, 50);
        }

        root.add(row).fillX().padBottom(4).colspan(2).row();
    }

    private void purchaseSkin(int skinIndex, int price) {
        Preferences prefs = Gdx.app.getPreferences(Constants.PREFS_NAME);
        int stars = prefs.getInteger(Constants.PREF_STARS, 0);
        if (stars < price) {
            if (game.sfxEnabled)
                game.manager.get("sounds/sfx/sfx_error.ogg", Sound.class).play(1.0f);
            return;
        }
        prefs.putInteger(Constants.PREF_STARS, stars - price);
        if (skinIndex == 1) prefs.putBoolean(Constants.PREF_SKIN_2_UNLOCKED, true);
        if (skinIndex == 2) prefs.putBoolean(Constants.PREF_SKIN_3_UNLOCKED, true);
        prefs.putInteger(Constants.PREF_SKIN, skinIndex);
        prefs.flush();
        if (game.sfxEnabled)
            game.manager.get("sounds/sfx/sfx_coin.ogg", Sound.class).play(1.0f);
        buildUI();
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

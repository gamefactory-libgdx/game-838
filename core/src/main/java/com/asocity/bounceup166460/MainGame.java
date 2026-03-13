package com.asocity.bounceup166460;

import com.asocity.bounceup166460.screens.MainMenuScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class MainGame extends Game {

    public SpriteBatch batch;
    public AssetManager manager;

    public BitmapFont fontTitle;
    public BitmapFont fontBody;
    public BitmapFont fontSmall;
    public BitmapFont fontHud;
    public BitmapFont fontScore;

    public boolean musicEnabled = true;
    public boolean sfxEnabled   = true;
    public Music   currentMusic = null;

    @Override
    public void create() {
        batch   = new SpriteBatch();
        manager = new AssetManager();

        generateFonts();

        setScreen(new MainMenuScreen(this));
    }

    private void generateFonts() {
        FreeTypeFontGenerator titleGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Orbitron-Regular.ttf"));
        FreeTypeFontGenerator bodyGen  = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = Constants.FONT_SIZE_TITLE;
        fontTitle = titleGen.generateFont(param);

        param.size = Constants.FONT_SIZE_SCORE;
        fontScore = titleGen.generateFont(param);

        param.size = Constants.FONT_SIZE_BODY;
        fontBody = bodyGen.generateFont(param);

        param.size = Constants.FONT_SIZE_SMALL;
        fontSmall = bodyGen.generateFont(param);

        param.size = Constants.FONT_SIZE_HUD;
        fontHud = bodyGen.generateFont(param);

        titleGen.dispose();
        bodyGen.dispose();
    }

    public void playMusic(String path) {
        Music requested = manager.get(path, Music.class);
        if (requested == currentMusic && currentMusic.isPlaying()) return;
        if (currentMusic != null) currentMusic.stop();
        currentMusic = requested;
        currentMusic.setLooping(true);
        currentMusic.setVolume(0.7f);
        if (musicEnabled) currentMusic.play();
    }

    public void playMusicOnce(String path) {
        if (currentMusic != null) currentMusic.stop();
        currentMusic = manager.get(path, Music.class);
        currentMusic.setLooping(false);
        currentMusic.setVolume(0.7f);
        if (musicEnabled) currentMusic.play();
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        manager.dispose();
        fontTitle.dispose();
        fontBody.dispose();
        fontSmall.dispose();
        fontHud.dispose();
        fontScore.dispose();
    }
}

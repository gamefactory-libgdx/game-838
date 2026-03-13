# CLAUDE.md — Global Rules for libGDX Android Games

> **READ THIS FIRST.** Before writing any code, read `GAME_SPEC.md` and `GDD.md`
> in this project root. All rules here apply to every game. Do not deviate.

---

## 1. Tech Stack

| Layer      | Technology                        |
|------------|-----------------------------------|
| Framework  | libGDX 1.14.0                     |
| Language   | Java 17                           |
| Build      | Gradle wrapper (./gradlew only)   |
| UI         | Scene2D exclusively               |
| Assets     | AssetManager exclusively          |
| Target     | Android only                      |

---

## 2. Exact Project Structure

```
project-root/
├── CLAUDE.md
├── GAME_SPEC.md
├── GDD.md
├── assets/                          ← all game assets go here
│   ├── backgrounds/
│   ├── fonts/                       ← Orbitron, Roboto (default) + PressStart2P, Kongtext, Xirod, Iomanoid, Vectroid, GaliverSans, Pixy, NoContinue
│   ├── sounds/                      ← music + sfx .ogg files (pre-copied by pipeline)
│   ├── ui/
│   └── sprites/                     ← Kenney CC0 sprites (pre-copied by pipeline)
│       └── SPRITES.md               ← manifest — READ THIS before using any sprite
├── core/
│   └── src/main/java/com/factory/GAME_SLUG/
│       ├── MainGame.java            ← extends Game
│       ├── Constants.java           ← ALL magic numbers
│       └── screens/
│           ├── MainMenuScreen.java
│           ├── SettingsScreen.java
│           ├── GameOverScreen.java
│           ├── LeaderboardScreen.java
│           └── ... (all other screens from GAME_SPEC)
├── android/
│   └── src/main/java/com/factory/GAME_SLUG/android/
│       └── AndroidLauncher.java     ← entry point only, do not modify
└── build.gradle
```

**IMPORTANT:** Replace `GAME_SLUG` with the actual game slug from GAME_SPEC.
Example: if slug is `brick-smash`, package is `com.asocity.bricksmash123456`
(The 6-digit suffix is appended by the pipeline — use whatever packageName is in GAME_SPEC.)
Java files go in: `core/src/main/java/com/factory/bricksmash/`

---

## 3. Screen Requirements — MANDATORY

Every game MUST have **between 8 and 10 screens**. Hard requirement.

### Always required:
| Screen | Purpose |
|--------|---------|
| `MainMenuScreen` | Title, play button, navigation hub |
| `SettingsScreen` | Music toggle, SFX toggle |
| `GameOverScreen` | Score, personal best, retry, menu |
| `LeaderboardScreen` | Local top 10 — SharedPreferences |

### Fill remaining 4-6 from GAME_SPEC screen list.

### Screen rules:
- Every screen is a separate Java class implementing `com.badlogic.gdx.Screen`
- Constructor receives the main `Game` instance
- Navigate via `game.setScreen(new XxxScreen(game))`
- Dispose old screen after setting new one

---

## 4. Physics Constants — MANDATORY

### Hold-to-fly / jetpack mechanic
If the game uses a "hold screen to fly/rise" mechanic (jetpack, flappy, balloon, rocket):
- `JETPACK_THRUST` (or equivalent) **MUST be at least 2× |GRAVITY|**
- With additive physics both forces apply every frame: net = THRUST + GRAVITY
- If THRUST < |GRAVITY| the player can never rise — game is unplayable

```java
// CORRECT — thrust overcomes gravity, net force is upward when holding
public static final float GRAVITY        = -900f;
public static final float JETPACK_THRUST = 1800f; // 2× gravity → net +900 upward
public static final float MAX_FALL_SPEED = -700f;

// WRONG — thrust less than gravity, player always falls even when holding
public static final float GRAVITY        = -900f;
public static final float JETPACK_THRUST =  600f; // net -300 → never rises
```

### Touch input in GameScreen with a HUD Stage
When a HUD `Stage` is in the `InputMultiplexer`, always poll input via `Gdx.input.isTouched()`
for game physics — never rely on Stage touch events for continuous hold detection.

```java
// CORRECT — polling works regardless of whether Stage consumed the event
boolean thrusting = Gdx.input.isTouched();

// WRONG — Stage touchDown fires once, not continuously while held
```

---

## 5. Sprites — MANDATORY

### How sprites work
The pipeline automatically copies a curated Kenney CC0 sprite library into
`assets/sprites/` before Claude runs. A manifest `assets/sprites/SPRITES.md`
is also written there, listing every available file.

**Before writing ANY game object code:**
1. Read `assets/sprites/SPRITES.md` — it is always present
2. Use the sprites listed there for player, enemies, obstacles, collectibles **and all UI**
3. Never use plain colored rectangles for game objects when sprites are available
4. Never use plain `TextButton` — always apply a button style using Kenney button PNGs

### Categories available
| Category | Sprites | Use for |
|----------|---------|---------|
| `runner/` | player walk/jump/hurt, enemies (slime, fly, bee, saw), obstacles (spike, rock, lava), ground tiles | Side-scrollers, runners, jetpack, flappy-style |
| `space/`  | player ships, enemy ships (4 types), lasers, asteroids | Space shooters, asteroid dodge |
| `racing/` | car_player (yellow), car_red, car_blue, car_green, car_black | Top-down car games |
| `puzzle/` | balls (blue/yellow/grey), paddles, back tiles | Brick breaker, ball games |
| `slingshot/` | wood/stone/metal/glass blocks (square/plank/column), alien targets, debris, star ratings | Angry Birds-style slingshot, physics destruction |
| `jumper/` | bunny player (2 skins), platforms (5 types + broken), flying/spike enemies, spring/jetpack items | Doodle Jump-style infinite vertical jumper |
| `generic/`| coin_gold, coin_silver, star, gem (4 colors) | Collectibles in any game |
| `ui/`     | buttons (5 colours × up/down), round icon buttons, slider, 21 HUD icons (incl. heart, timer) | **Every screen** — all buttons, HUD icons, settings sliders |

### Loading sprites with AssetManager

```java
// In your loading screen or MainGame:
manager.load("sprites/player_idle.png", Texture.class);
manager.load("sprites/player_walk1.png", Texture.class);
manager.load("sprites/enemy_slime.png", Texture.class);
manager.load("sprites/coin_gold.png", Texture.class);
// ... load all sprites you'll use

// In your screen (after finishLoading):
Texture playerTex = manager.get("sprites/player_idle.png", Texture.class);
```

### Animation example (walk cycle)

```java
// Declare
private Animation<TextureRegion> walkAnim;
private float stateTime = 0f;

// In constructor (after assets loaded):
TextureRegion[] frames = {
    new TextureRegion(manager.get("sprites/player_walk1.png", Texture.class)),
    new TextureRegion(manager.get("sprites/player_walk2.png", Texture.class))
};
walkAnim = new Animation<>(0.15f, frames);

// In render():
stateTime += delta;
TextureRegion frame = walkAnim.getKeyFrame(stateTime, true);
batch.draw(frame, x, y, width, height);
```

### UI button rules — MANDATORY
Every screen must use styled buttons. Never use unstyled `TextButton`.

```java
// Helper — reuse across all screens (define once in MainGame or a UIFactory):
private TextButton.TextButtonStyle makeButtonStyle(String upFile, String downFile) {
    TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
    s.font      = game.skin.getFont("default-font"); // your BitmapFont
    s.up        = new TextureRegionDrawable(manager.get(upFile,   Texture.class));
    s.down      = new TextureRegionDrawable(manager.get(downFile, Texture.class));
    s.fontColor = Color.WHITE;
    return s;
}

// Primary action (Play, Start, Continue):
TextButton playBtn = new TextButton("PLAY",
    makeButtonStyle("sprites/button_blue.png", "sprites/button_blue_pressed.png"));
playBtn.setSize(240, 70);

// Secondary / Back / Menu:
TextButton backBtn = new TextButton("BACK",
    makeButtonStyle("sprites/button_grey.png", "sprites/button_grey_pressed.png"));
backBtn.setSize(200, 60);

// Buy / Confirm in ShopScreen:
TextButton buyBtn = new TextButton("BUY",
    makeButtonStyle("sprites/button_green.png", "sprites/button_green_pressed.png"));

// Retry / Danger:
TextButton retryBtn = new TextButton("RETRY",
    makeButtonStyle("sprites/button_red.png", "sprites/button_red_pressed.png"));
```

Standard button sizes: main 240×70 · secondary 200×60 · round icon 60×60

### Settings screen — use icons for toggles
```java
// Music toggle button using icon sprites:
ImageButton musicBtn = new ImageButton(
    new TextureRegionDrawable(manager.get("sprites/icon_music_on.png",  Texture.class)),
    new TextureRegionDrawable(manager.get("sprites/icon_music_off.png", Texture.class))
);
musicBtn.setChecked(!musicEnabled); // checked state = off icon shown
```

### HUD icons — MANDATORY source
All HUD icons (hearts, timer, star, settings gear, etc.) **must** come from `assets/sprites/`.
**Never** create an `assets/icons/` folder — it is redundant and will be ignored.

| HUD need | Use this sprite |
|----------|----------------|
| Life / health indicator | `sprites/icon_heart.png` / `sprites/icon_heart_empty.png` |
| Timer / countdown | `sprites/icon_timer.png` |
| Star / rating | `sprites/icon_star.png` |
| Settings gear | `sprites/icon_settings.png` |
| Lock / unlock | `sprites/icon_locked.png` / `sprites/icon_unlocked.png` |
| Leaderboard | `sprites/icon_leaderboard.png` |
| Trophy | `sprites/icon_trophy.png` |
| Close / X | `sprites/icon_close.png` |

Game-specific power-up icons (e.g. `ui/power_up_laser_icon.png` from Figma) may be loaded
from `assets/ui/` — those are exported by Figma and are game-specific, **not** generic HUD icons.

```java
// WRONG — never load from assets/icons/
manager.load("icons/heart.png", Texture.class);

// CORRECT — use the sprites/ library
manager.load("sprites/icon_heart.png", Texture.class);
manager.load("sprites/icon_timer.png", Texture.class);
```

### Other important sprite rules
- Load sprites via `AssetManager` — zero `new Texture("sprites/...")` calls
- Scale sprites to world units (not pixel size): typical player is 64×64 world units
- If a sprite category doesn't match the game theme, use `generic/` collectibles
  and draw obstacles/terrain using `ShapeRenderer` as fallback

---

## 6. Fonts — MANDATORY

The pipeline pre-copies two TTF fonts into `assets/fonts/` before Claude runs.

### Available fonts

The pipeline pre-copies these fonts. The **default** pair is Orbitron (title) + Roboto (body) — always available.
Additional fonts are available for creative variety — pick one pair per game.

#### Default (always use unless you have a specific style reason)

| File | Style | Use for |
|------|-------|---------|
| `fonts/Roboto-Regular.ttf` | Clean sans-serif | Body text, labels, button labels, HUD values |
| `fonts/Orbitron-Regular.ttf` | Futuristic rounded | Titles, score display, game-over headings |

#### Pixel / Retro (for retro/arcade/chiptune games)

| File | Style | Use for |
|------|-------|---------|
| `fonts/PressStart2P.ttf` | Classic 8-bit pixel | Retro arcade titles, score display |
| `fonts/Kongtext.ttf` | Bold pixel | Game over text, chunky labels |
| `fonts/Pixy.ttf` | Small pixel | Compact HUD values, pixel-art games |
| `fonts/NoContinue.ttf` | Pixel rounded | Menu buttons, casual arcade style |

#### Sci-fi / Futuristic (for space, tech, racing games)

| File | Style | Use for |
|------|-------|---------|
| `fonts/Xirod.otf` | Tech angular | Sci-fi titles, shooter HUD |
| `fonts/Iomanoid.otf` | Tech outlined | Space game titles, tech UI |
| `fonts/Vectroid.otf` | Vector retro | Space/neon game titles |

#### Clean Display (for casual, sports, racing games)

| File | Style | Use for |
|------|-------|---------|
| `fonts/GaliverSans.ttf` | Clean bold caps | Racing / sports titles |

**Font selection rule:** Pick 2 fonts max per game — one for titles (larger), one for body/buttons (smaller).
**NEVER load fonts from `assets/ui/` or any other directory — they are always in `assets/fonts/`.**

### Loading fonts — FreeTypeFontGenerator pattern

Fonts are TTF files and must be converted to `BitmapFont` at runtime using `FreeTypeFontGenerator`.
Do this once in `MainGame.create()` and store the results as public fields so all screens can share them.

```java
// In MainGame — public fields
public BitmapFont fontBody;   // Roboto — labels, buttons, HUD
public BitmapFont fontTitle;  // Orbitron — titles, scores

// In MainGame.create() — generate fonts BEFORE loading other assets
FreeTypeFontGenerator bodyGen  = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
FreeTypeFontGenerator titleGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Orbitron-Regular.ttf"));

FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

param.size = 28;
fontBody = bodyGen.generateFont(param);

param.size = 48;
fontTitle = titleGen.generateFont(param);

bodyGen.dispose();
titleGen.dispose();
```

Generate additional sizes as needed (e.g. `param.size = 20` for small labels, `param.size = 72` for big score).
Always dispose the generator immediately after generating — keep the `BitmapFont`, not the generator.

### Using fonts in button styles

```java
// CORRECT — use game.fontBody, not skin.getFont()
private TextButton.TextButtonStyle makeButtonStyle(String upFile, String downFile) {
    TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
    s.font      = game.fontBody;   // ← always reference the shared font field
    s.up        = new TextureRegionDrawable(manager.get(upFile,   Texture.class));
    s.down      = new TextureRegionDrawable(manager.get(downFile, Texture.class));
    s.fontColor = Color.WHITE;
    return s;
}

// WRONG — skin.getFont() fails if no skin is loaded
s.font = game.skin.getFont("default-font");
```

### Dispose in MainGame.dispose()

```java
@Override
public void dispose() {
    fontBody.dispose();
    fontTitle.dispose();
    manager.dispose();
}
```

### Required Gradle dependency

The `gdx-freetype` extension must be present in `core/build.gradle`:
```groovy
dependencies {
    api "com.badlogicgames.gdx:gdx-freetype:$gdxVersion"
}
```
And in `android/build.gradle`:
```groovy
natives "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi-v7a"
natives "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-arm64-v8a"
natives "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86"
natives "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86_64"
```

---

## 7. Game Mechanics Consistency — MANDATORY

### Coins / Currency — CRITICAL
If the game collects coins, gems, or any in-game currency:
- The player MUST be able to spend them → always implement a `ShopScreen`
- `ShopScreen` is reachable from `MainMenuScreen` and has a "Main Menu" back button
- Coin balance persists via SharedPreferences

If the GAME_SPEC does **not** define a shop, do **not** add coin collection.
Replace coin pickups with direct score bonuses instead:

```java
// WRONG — coins that go nowhere (no shop = meaningless mechanic)
collectibles.add(new Coin(x, y));

// CORRECT option B — no shop → score bonus instead
score += Constants.COIN_SCORE_VALUE; // direct score, no currency stored
```

### ShopScreen — MANDATORY content
Every `ShopScreen` MUST have **exactly these two categories**:

**1. Power-ups** (3 items — pure code, no extra assets needed):

| Item | Price | Effect |
|------|-------|--------|
| Shield | 20 coins | 5 seconds invincibility on next run |
| Coin Magnet | 30 coins | Auto-collects nearby coins for 10s |
| 2× Score | 50 coins | Double all points for the next run |

**2. Character Skins** (for runner/platformer/jetpack/space games):

For **runner/jetpack/platformer** games — 3 skins using the sprite variants in `assets/sprites/`:

| Skin | Price | Sprites to use |
|------|-------|---------------|
| Blue Alien (default) | free | `player_idle.png`, `player_walk1.png`, `player_walk2.png`, `player_jump.png` |
| Green Alien | 100 coins | `player_idle_green.png`, `player_walk1_green.png`, etc. |
| Pink Alien | 150 coins | `player_idle_pink.png`, `player_walk1_pink.png`, etc. |

For **racing** games — 3 car skins:

| Skin | Price | Sprite |
|------|-------|--------|
| Yellow Car (default) | free | `car_player.png` |
| Red Car | 100 coins | `car_red.png` |
| Blue Car | 150 coins | `car_blue.png` |

For **space** games — 2 ship skins:

| Skin | Price | Sprite |
|------|-------|--------|
| Blue Ship (default) | free | `player_ship.png` |
| Green Ship | 100 coins | `player_ship_alt.png` |

### Implementing skins — SharedPreferences pattern

```java
// Save selected skin index (0 = default, 1 = skin2, 2 = skin3)
Preferences prefs = Gdx.app.getPreferences("GamePrefs");
prefs.putInteger(Constants.PREF_SKIN, selectedSkinIndex);
prefs.flush();

// In GameScreen constructor — load correct player sprites based on saved skin:
int skin = Gdx.app.getPreferences("GamePrefs").getInteger(Constants.PREF_SKIN, 0);
String[] idleFrames = {
    "sprites/player_idle.png",       // skin 0 — yellow (default)
    "sprites/player_idle_green.png", // skin 1 — green
    "sprites/player_idle_pink.png"   // skin 2 — pink
};
Texture playerTex = manager.get(idleFrames[skin], Texture.class);
```

### Collision Detection — CRITICAL
Every visual element that should kill/hurt the player **MUST** have an active collision `Rectangle`.
Background decorations (ambient shapes, parallax art, hexagons, panels) must **NEVER** have collision rectangles.

**Rule: if it should kill you → it has a Rectangle. If it is decoration → no Rectangle.**

```java
// Maintain two SEPARATE lists — never mix them
private Array<Rectangle> obstacles;    // COLLIDABLE — kills player on contact
private Array<ShapeData>  decorations; // VISUAL ONLY — never collision-tested

// In update():
for (Rectangle obs : obstacles) {
    if (playerRect.overlaps(obs)) {
        triggerGameOver();
    }
}
// decorations are only drawn — never checked for collision
```

Common mistake: drawing background shapes with `ShapeRenderer` and accidentally adding
them to the obstacle list. Always ask: "Is this meant to kill the player?"
- Yes → add to `obstacles` list
- No  → draw only, never in `obstacles`

---

## 8. Navigation Rules — MANDATORY

These are hard requirements based on real bugs. Violating them causes broken UX.

### Every screen MUST have a "Main Menu" button
- No screen is a dead end
- **Every** screen — `GameScreen`, `PauseScreen`, `GameOverScreen`, `LeaderboardScreen`,
  `SettingsScreen`, `ShopScreen`, level complete — must have a clearly visible button
  that returns to `MainMenuScreen`
- `LeaderboardScreen` is the most frequently forgotten — it **always** needs a back button
- The Android system back button must also navigate to `MainMenuScreen`, never exit the app

```java
// CORRECT — handle back button on every screen
@Override
public boolean keyDown(int keycode) {
    if (keycode == Input.Keys.BACK) {
        game.setScreen(new MainMenuScreen(game));
        return true;
    }
    return false;
}
```

### Restart / Game Over mechanics — CRITICAL
- After game over, game state MUST be fully reset before restarting
- Always create a NEW `GameScreen` instance on retry — never reuse the old one
- Ball/player/projectile must start from initial spawn position with zero velocity

```java
// CORRECT — restart creates fresh screen
retryButton.addListener(new ChangeListener() {
    public void changed(ChangeEvent event, Actor actor) {
        game.setScreen(new GameScreen(game)); // NEW instance every time
    }
});

// WRONG — reusing old state causes ball stuck, input broken
retryButton.addListener(new ChangeListener() {
    public void changed(ChangeEvent event, Actor actor) {
        game.setScreen(gameScreen); // OLD instance — never do this
    }
});
```

### Pause screen — MANDATORY
- Every game screen must have a visible pause button during gameplay
- Pause must freeze all game logic (delta time ignored while paused)
- Pause screen must have: Resume, Restart, Main Menu buttons

---

## 9. Viewport — MANDATORY — No Black Bars

Every screen MUST use `StretchViewport` to fill the entire screen with no black bars.

```java
// In every screen — declare fields
private OrthographicCamera camera;
private Viewport viewport;
private Stage stage;

// In constructor
camera = new OrthographicCamera();
viewport = new StretchViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
stage = new Stage(viewport, game.batch);
Gdx.input.setInputProcessor(stage);

// resize() — MANDATORY in every screen
@Override
public void resize(int width, int height) {
    viewport.update(width, height, true);
}
```

Define in `Constants.java`:
```java
public static final float WORLD_WIDTH  = 480f;
public static final float WORLD_HEIGHT = 854f;  // 16:9 portrait
```

All game coordinates use world units (480x854), never raw pixel values.

---

## 10. Location / World Variants

If the game has multiple environments:
- Each gets its own `GameScreen` subclass
- Each has a unique background asset
- Each has at least one unique obstacle/hazard type
- Share base logic via a parent class (e.g. `BaseGameScreen extends ScreenAdapter`)

---

## 11. AndroidLauncher.java

Create `android/src/main/java/com/factory/GAME_SLUG/android/AndroidLauncher.java`
(replace `GAME_SLUG` with the actual slug, dots removed — same as the package name).

```java
package com.asocity.GAME_SLUG.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.asocity.GAME_SLUG.MainGame;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        initialize(new MainGame(), configuration);
    }
}
```

The applicationId in `android/build.gradle` must match the package name exactly.
Set it to the `packageName` from GAME_SPEC (format: `com.asocity.{slug}{6digits}`).

---

## 12. Audio — MANDATORY

Every game MUST have background music and sound effects. They are pre-copied to
`assets/sounds/` by the pipeline. Read `assets/sounds/SOUNDS.md` for the full file list.

### Fields in MainGame (shared state)

```java
public boolean musicEnabled = true;
public boolean sfxEnabled   = true;
public Music   currentMusic = null;
```

### Loading (in LoadingScreen or MainGame.create)

```java
// Music — use Music class (streaming)
manager.load("sounds/music/music_menu.ogg",           Music.class);
manager.load("sounds/music/music_gameplay.ogg",       Music.class);
manager.load("sounds/music/music_gameplay_alt.ogg",   Music.class);
manager.load("sounds/music/music_gameplay_space.ogg", Music.class);
manager.load("sounds/music/music_game_over.ogg",      Music.class);

// SFX — use Sound class (buffered)
manager.load("sounds/sfx/sfx_button_click.ogg",   Sound.class);
manager.load("sounds/sfx/sfx_button_back.ogg",    Sound.class);
manager.load("sounds/sfx/sfx_toggle.ogg",         Sound.class);
manager.load("sounds/sfx/sfx_coin.ogg",           Sound.class);
manager.load("sounds/sfx/sfx_jump.ogg",           Sound.class);
manager.load("sounds/sfx/sfx_hit.ogg",            Sound.class);
manager.load("sounds/sfx/sfx_game_over.ogg",      Sound.class);
manager.load("sounds/sfx/sfx_level_complete.ogg", Sound.class);
manager.load("sounds/sfx/sfx_power_up.ogg",       Sound.class);
manager.load("sounds/sfx/sfx_shoot.ogg",          Sound.class);
```

### Playing music — helpers in MainGame

```java
// For menu and gameplay screens — loops forever
// CRITICAL: check if same track is already playing — do NOT restart it.
// This prevents music restarting from the beginning when navigating to
// SettingsScreen, LeaderboardScreen, ShopScreen, etc. that share the same track.
public void playMusic(String path) {
    Music requested = manager.get(path, Music.class);
    if (requested == currentMusic && currentMusic.isPlaying()) return; // already playing — do nothing
    if (currentMusic != null) currentMusic.stop();
    currentMusic = requested;
    currentMusic.setLooping(true);
    currentMusic.setVolume(0.7f);
    if (musicEnabled) currentMusic.play();
}

// For game over screen — plays ONCE then stops (never loops)
public void playMusicOnce(String path) {
    if (currentMusic != null) currentMusic.stop();
    currentMusic = manager.get(path, Music.class);
    currentMusic.setLooping(false);  // CRITICAL: game over must NOT loop
    currentMusic.setVolume(0.7f);
    if (musicEnabled) currentMusic.play();
}
```

Usage per screen:
```java
// MainMenuScreen constructor:
game.playMusic("sounds/music/music_menu.ogg");

// GameScreen constructor — general (runners, puzzles, racing):
game.playMusic("sounds/music/music_gameplay.ogg");

// GameScreen constructor — space shooter games:
game.playMusic("sounds/music/music_gameplay_space.ogg");

// GameOverScreen constructor — plays ONCE, does NOT loop:
game.playMusicOnce("sounds/music/music_game_over.ogg");
```

### Playing SFX

```java
// Helper — call wherever needed:
private void playSound(String path) {
    if (game.sfxEnabled)
        game.manager.get(path, Sound.class).play(1.0f);
}

// Examples:
playSound("sounds/sfx/sfx_button_click.ogg");  // every button listener
playSound("sounds/sfx/sfx_coin.ogg");           // on coin collect
playSound("sounds/sfx/sfx_jump.ogg");           // on jump/thrust
playSound("sounds/sfx/sfx_hit.ogg");            // on player hurt
playSound("sounds/sfx/sfx_game_over.ogg");      // on game over
```

### Settings screen — music/sfx toggles MUST actually work

```java
// Load saved state in every screen constructor:
Preferences prefs = Gdx.app.getPreferences("GamePrefs");
game.musicEnabled = prefs.getBoolean(Constants.PREF_MUSIC, true);
game.sfxEnabled   = prefs.getBoolean(Constants.PREF_SFX,   true);

// Toggle listener:
musicBtn.addListener(new ChangeListener() {
    public void changed(ChangeEvent e, Actor a) {
        game.musicEnabled = !game.musicEnabled;
        prefs.putBoolean(Constants.PREF_MUSIC, game.musicEnabled);
        prefs.flush();
        if (game.currentMusic != null) {
            if (game.musicEnabled) game.currentMusic.play();
            else game.currentMusic.pause();
        }
        playSound("sounds/sfx/sfx_toggle.ogg");
    }
});
```

### Constants required

```java
public static final String PREF_MUSIC = "musicEnabled";
public static final String PREF_SFX   = "sfxEnabled";
```

### Rules
- Always use `Music` for background tracks, `Sound` for SFX — never swap them
- Always load via `AssetManager` — never `Gdx.audio.newSound()` or `Gdx.audio.newMusic()`
- Menu / gameplay music must loop: `setLooping(true)` via `playMusic()`
- **Game over music must NOT loop**: use `playMusicOnce()` — `setLooping(false)`
- Stop current music before starting new: check `currentMusic != null`
- SFX volume: `play(1.0f)` — use `play(0.5f)` for quieter effects like toggle

---

## 13. Assets

```java
// CORRECT
manager.load("backgrounds/bg_desert.png", Texture.class);
manager.finishLoading();
Texture bg = manager.get("backgrounds/bg_desert.png", Texture.class);

// WRONG — never do this
Texture bg = new Texture("backgrounds/bg_desert.png");
```

- All filenames exactly as in GAME_SPEC
- If AssetManager owns it, use `manager.unload()` not `texture.dispose()`

---

## 14. Constants.java

All magic numbers go here — speeds, sizes, timings, score values,
SharedPreferences keys, world dimensions. Never hardcode numbers inline.

```java
public class Constants {
    public static final float WORLD_WIDTH  = 480f;
    public static final float WORLD_HEIGHT = 854f;
    // ... all other constants
}
```

---

## 15. Build

```bash
# Phase 1 — fix loop until debug passes:
./gradlew android:assembleDebug

# Phase 2 — once debug is GREEN, run release bundle:
./gradlew android:bundleRelease
```

- Zero errors, zero warnings
- Gradle wrapper only — never system gradle
- Fix ALL errors before finishing
- The `signingConfigs.release` block is already in `android/build.gradle` — do NOT modify it

---

## 16. Data Persistence

```java
Preferences prefs = Gdx.app.getPreferences("GamePrefs");
prefs.putInteger("highScore", score);
prefs.flush();
```

Keys defined in `Constants.java`. Save: high scores, unlocks, settings.

---

## 17. Reference Games

If reference games are provided in the prompt, they are at:
```
/home/kaliuzhnyi/asocity/reference/<game-folder>/
```

Study them BEFORE writing any code:
- Read their `core/src/` Java files to understand class structure
- Look at how screens are organized and navigation implemented
- Look at how `AssetManager` is loaded and disposed
- Look at how input is handled per screen
- Match their code quality, architecture, and scope

If no reference games provided — build clean, minimal implementation matching the GDD archetype.

---

## 18. Completion Checklist

- [ ] `./gradlew android:assembleDebug` → BUILD SUCCESSFUL
- [ ] `./gradlew android:bundleRelease` → BUILD SUCCESSFUL
- [ ] Exactly 8-10 screens implemented
- [ ] All screens in GAME_SPEC implemented
- [ ] All screens navigate correctly per GAME_SPEC screen flow
- [ ] Every screen has a visible "Main Menu" button
- [ ] Android back button navigates to MainMenuScreen on every screen — never exits app
- [ ] Game Over retry creates a NEW GameScreen instance — never reuses old state
- [ ] Ball/player starts from spawn position with zero velocity on restart
- [ ] Pause screen implemented with Resume / Restart / Main Menu
- [ ] Every screen uses StretchViewport — no black bars
- [ ] All assets in GAME_SPEC exist in `assets/`
- [ ] All assets loaded via AssetManager — zero `new Texture()` calls
- [ ] Fonts loaded from `assets/fonts/` — never from `assets/ui/` or anywhere else
- [ ] `game.fontBody` (Roboto) and `game.fontTitle` (Orbitron) generated in `MainGame.create()`
- [ ] Button styles use `game.fontBody` — never `skin.getFont()`
- [ ] `Constants.java` has WORLD_WIDTH, WORLD_HEIGHT and all magic numbers
- [ ] SharedPreferences saves/loads scores and settings
- [ ] No `System.out.println` — use `Gdx.app.log`
- [ ] `dispose()` on every screen
- [ ] Background music plays on every screen (menu, gameplay, game over)
- [ ] GameScreen uses `music_gameplay_space.ogg` for space shooter games, `music_gameplay.ogg` for all others
- [ ] GameOverScreen uses `playMusicOnce()` — game over music plays once and stops, never loops
- [ ] SFX plays on button clicks, coin collect, jump, hit, game over
- [ ] Music/SFX toggles in SettingsScreen actually pause/resume audio
- [ ] `playMusic()` uses same-track guard — music does NOT restart when navigating between screens that share the same track
- [ ] `PREF_MUSIC` and `PREF_SFX` saved to SharedPreferences
- [ ] Package name matches `packageName` from GAME_SPEC (`com.asocity.{slug}{6digits}`)
- [ ] `applicationId` in `android/build.gradle` updated to match package name (com.asocity.*)
- [ ] android/res/values/strings.xml app_name updated to game title
- [ ] `assets/sprites/SPRITES.md` was read before writing game object code
- [ ] Player, enemies, collectibles use sprites from `assets/sprites/` — no plain rectangles
- [ ] All buttons use Kenney button PNGs (`button_blue.png` etc.) — no unstyled `TextButton`
- [ ] Every button has BOTH `up` (depth shadow) and `down` (flat) drawable set
- [ ] Settings screen uses `icon_music_on/off` and `icon_sfx_on/off` for toggles
- [ ] If coins/currency collected → `ShopScreen` exists with spendable items
- [ ] If no `ShopScreen` → no coin collection; collectibles add score directly
- [ ] `LeaderboardScreen` has a visible "Main Menu" / "Back" button
- [ ] Every obstacle that can kill the player has a collision `Rectangle`
- [ ] Background decorations (ambient shapes, parallax art) have NO collision rectangles
- [ ] Collidable vs decorative objects kept in separate lists — never mixed

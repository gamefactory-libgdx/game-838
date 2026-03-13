# Add Screen

Add a new Screen class to this libGDX game following CLAUDE.md rules exactly.

**Arguments:** $ARGUMENTS (screen name, e.g. "ShopScreen" or "TutorialScreen")

**Steps:**
1. Read CLAUDE.md and GAME_SPEC.md to understand the expected screen list and navigation
2. Create a new file: `core/src/main/java/com/factory/SLUG/screens/SCREENNAME.java`
3. The class must implement `com.badlogic.gdx.Screen`
4. Use `StretchViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera)` - no exceptions
5. Must have a visible Main Menu button: `game.setScreen(new MainMenuScreen(game))`
6. Android back button must navigate to MainMenuScreen via InputMultiplexer + InputAdapter
7. All assets loaded via AssetManager - never `new Texture()`
8. Implement `resize(int w, int h) { viewport.update(w, h, true); }`
9. Implement `dispose()` - dispose stage, manager, all fonts
10. Wire navigation FROM existing screens TO this new screen as needed
11. Run `./gradlew android:assembleDebug --no-daemon` and fix all errors

# Audit Navigation

Check every screen meets CLAUDE.md navigation requirements.

**Requirements per screen:**
1. Main Menu button - every screen except MainMenuScreen must navigate to `new MainMenuScreen(game)`
2. Back button - every screen must handle `Input.Keys.BACK` going to MainMenuScreen
3. Pause button - every GameScreen / level screen must have a visible pause button
4. Pause screen - must have Resume, Restart, Main Menu buttons
5. Game Over retry - must create NEW GameScreen instance, never reuse old state
6. No dead ends - every screen has at least one navigation button

**Steps:**
1. List all Screen classes: `find core/src -name "*Screen.java" | sort`
2. Read each screen file and check the requirements above
3. Report any screen that is missing required navigation
4. Fix each missing navigation element
5. Run `./gradlew android:assembleDebug --no-daemon` to confirm build passes

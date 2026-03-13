# Check Assets

Verify all assets referenced in Java source code actually exist in the assets/ directory.

**Steps:**
1. List actual assets: `find assets/ -name "*.png" -o -name "*.ttf" | sort`
2. Find referenced assets in code: `grep -rn '"backgrounds/\|"ui/\|"fonts/' core/src/`
3. Compare: any path in Java but NOT in assets/ is missing
4. For each missing asset:
   - Font (*.ttf): use an alternative from assets/fonts/ and update the code
   - Background or UI PNG: do NOT generate a placeholder; use bg_main.png as fallback if needed
5. Report all missing assets and actions taken
6. Run `./gradlew android:assembleDebug --no-daemon` to confirm build still passes

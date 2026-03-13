# Fix Build

Run `./gradlew android:assembleDebug --no-daemon` and fix ALL compilation errors until the build is clean.

**Loop until BUILD SUCCESSFUL:**
1. Run: `./gradlew android:assembleDebug --no-daemon 2>&1 | tail -40`
2. If errors exist, read each failing file and fix the issue
3. Repeat until exit code 0 and BUILD SUCCESSFUL appears

**Common fixes:**
- `illegal start of type` - orphaned code outside a class body; remove it
- `cannot find symbol` - missing import or wrong class name; add the correct import
- `method does not override` - @Override on a method not in the interface; remove @Override
- Package mismatch - class declares wrong package; fix to match directory path
- com.factory.template references - delete both:
    `rm -rf core/src/main/java/com/factory/template/`
    `rm -rf android/src/main/java/com/factory/template/`

After BUILD SUCCESSFUL, report APK path: `android/build/outputs/apk/debug/android-debug.apk`

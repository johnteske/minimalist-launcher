#!/bin/sh
#
# https://gist.github.com/aldemirenes/9bbea36abba33d74196ca5e488c4365e

#package_name=launcher.minimalist.com
package_name=launcher.simple.com.simplelauncher

./gradlew assembleDebug
adb uninstall "$package_name" # launcher.minimalist.com
adb -d install -r MinimalistLauncher/build/outputs/apk/debug/MinimalistLauncher-debug.apk

adb shell monkey -p "$package_name" -c android.intent.category.LAUNCHER 1

./logcat.sh "$package_name"

#!/bin/sh

package_name=$1

app_pid=$(adb shell ps | grep "$package_name" | awk '{ print $2 }')
adb logcat | grep "$app_pid"

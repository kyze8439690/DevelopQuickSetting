import os

print os.popen('adb root').read()
print os.popen('adb remount').read()
print os.popen('adb push app/build/outputs/apk/app-debug.apk /system/priv-app/app/').read()
print os.popen('adb shell chmod 644 /system/priv-app/app/app-debug.apk').read()
print os.popen('adb shell pkill zygote').read()

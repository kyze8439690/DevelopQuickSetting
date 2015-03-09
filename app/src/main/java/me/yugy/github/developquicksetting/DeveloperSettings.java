package me.yugy.github.developquicksetting;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DeveloperSetting class, method may block ui thread
 * (every method spend about 20-30ms to execute, so remember to call it asynchronously.
 */
public class DeveloperSettings {

    private static final boolean LOG_ENABLED = false;

    public static boolean isAdbEnabled(Context context) {
        if (context == null) {
            return false;
        }
        long startTime = System.currentTimeMillis();
        int isAdbChecked = Settings.Global.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
        if (LOG_ENABLED) {
            Utils.log("isAdbEnabled spends " + (System.currentTimeMillis() - startTime) + "ms.");
        }
        return isAdbChecked == 1;
    }

    public static boolean isDebugLayoutEnabled() throws IOException {
        long startTime = System.currentTimeMillis();
        String result = SystemProperties.get(Property.DEBUG_LAYOUT_PROPERTY, "false");
        if (LOG_ENABLED) {
            Utils.log("isDebugLayoutEnabled spends " + (System.currentTimeMillis() - startTime) + "ms.");
        }
        return "true".equals(result);
    }

    public static boolean isShowOverdrawEnabled() throws IOException {
        long startTime = System.currentTimeMillis();
        String result = SystemProperties.get(Property.getDebugOverdrawPropertyKey(), "false");
        if (LOG_ENABLED) {
            Utils.log("isShowOverdrawEnabled spends " + (System.currentTimeMillis() - startTime) + "ms.");
        }
        return Property.getDebugOverdrawPropertyEnabledValue().equals(result);
    }

    public static boolean isShowProfileGPURendering() throws IOException {
        long startTime = System.currentTimeMillis();
        String result = SystemProperties.get(Property.PROFILE_PROPERTY, "false");
        if (LOG_ENABLED) {
            Utils.log("isShowProfileGPURendering spends " + (System.currentTimeMillis() - startTime) + "ms.");
        }
        return "visual_bars".equals(result);
    }

    public static boolean isImmediatelyDestroyActivities(Context context) {
        if (context == null) {
            return false;
        }
        long startTime = System.currentTimeMillis();
        int isAlwaysDestroyActivitiesChecked = Settings.Global.getInt(
                context.getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);
        if (LOG_ENABLED) {
            Utils.log("isImmediatelyDestroyActivities spends " + (System.currentTimeMillis() - startTime) + "ms.");
        }
        return isAlwaysDestroyActivitiesChecked == 1;
    }

    public static String getAdbThroughWifiPort() throws IOException {
        return SystemProperties.get(Property.ADB_WIFI_PORT, "-1");
    }

    public static boolean isAdbThroughWifiEnabled() throws IOException {
        long startTime = System.currentTimeMillis();
        String result = getAdbThroughWifiPort();
        if (LOG_ENABLED) {
            Utils.log("isAdbThroughWifiEnabled spends " + (System.currentTimeMillis() - startTime) + "ms.");
        }
        return result.matches("[0-9]+");
    }

    public static boolean setDebugLayoutEnabled(boolean enabled) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream output = new DataOutputStream(process.getOutputStream());
        output.writeBytes("setprop " + Property.DEBUG_LAYOUT_PROPERTY + " " + (enabled ? "true" : "false") + "\n");
        output.writeBytes("exit\n");
        output.flush();
        process.waitFor();
        output.close();
        boolean result = process.exitValue() == 0;
        pokeSystemProperties();
        return result;
    }

    public static boolean setShowOverdrawEnabled(boolean enabled) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream output = new DataOutputStream(process.getOutputStream());
        output.writeBytes("setprop " + Property.getDebugOverdrawPropertyKey() + " "
                + (enabled ? Property.getDebugOverdrawPropertyEnabledValue() : "false") + "\n");
        output.writeBytes("exit\n");
        output.flush();
        process.waitFor();
        output.close();
        boolean result = process.exitValue() == 0;
        pokeSystemProperties();
        return result;
    }

    public static boolean setProfileGPURenderingEnabled(boolean enabled) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream output = new DataOutputStream(process.getOutputStream());
        output.writeBytes("setprop " + Property.PROFILE_PROPERTY + " " + (enabled ? "visual_bars" : "false") + "\n");
        output.writeBytes("exit\n");
        output.flush();
        process.waitFor();
        output.close();
        boolean result = process.exitValue() == 0;
        pokeSystemProperties();
        return result;
    }

    public static boolean setImmediatelyDestroyActivities(Context context, boolean enabled)
            throws IOException, InterruptedException {
        //this piece is not work, throw a InvocationTargetException on android 5.0
        // that says "Caused by: java.lang.SecurityException: Package android does not belong to 10076"

//                Class activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
//                Method getDefaultMethod = activityManagerNativeClass.getMethod("getDefault");
//                Object activityManagerNativeInstance = getDefaultMethod.invoke(null);
//                Method setAlwaysFinishMethod = activityManagerNativeClass.getMethod("setAlwaysFinish", boolean.class);
//                setAlwaysFinishMethod.invoke(activityManagerNativeInstance, isChecked);

        boolean result = Settings.Global.putInt(
                context.getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES,
                enabled ? 1 : 0);
        pokeSystemProperties();
        return result;
    }

    public static boolean setAdbThroughWIFIEnabled(boolean enabled) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream output = new DataOutputStream(process.getOutputStream());
        output.writeBytes("setprop " + Property.ADB_WIFI_PORT + " " + (enabled ? Conf.ADB_WIFI_PORT : -1) + "\n");
        output.writeBytes("stop adbd\n");
        output.writeBytes("start adbd\n");
        output.writeBytes("exit\n");
        output.flush();
        process.waitFor();
        output.close();
        return process.exitValue() == 0;
    }

    public static boolean toggleDebugLayout() throws IOException, InterruptedException {
        return setDebugLayoutEnabled(!isDebugLayoutEnabled());
    }

    public static boolean toggleShowOverdraw() throws IOException, InterruptedException {
        return setShowOverdrawEnabled(!isShowOverdrawEnabled());
    }

    public static boolean toggleProfileGPURendering() throws IOException, InterruptedException {
        return setProfileGPURenderingEnabled(!isShowProfileGPURendering());
    }

    public static boolean toggleImmediatelyDestroyActivity(Context context) throws IOException, InterruptedException {
        return setImmediatelyDestroyActivities(context, !isImmediatelyDestroyActivities(context));
    }

    public static boolean toggleAdbThroughWifi() throws IOException, InterruptedException {
        return setAdbThroughWIFIEnabled(!isAdbThroughWifiEnabled());
    }

    private static void pokeSystemProperties() {
        new SystemPropPoker().execute();
    }

    public static String getWifiIp() throws IOException {
        Process process = Runtime.getRuntime().exec("ip -f inet addr show wlan0");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String result = builder.toString();
        Pattern pattern = Pattern.compile("inet ([.0-9]+)");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find(1)) {
            return matcher.group(1);
        } else {
            return null;                    //maybe because wifi is not opened.
        }
    }

}

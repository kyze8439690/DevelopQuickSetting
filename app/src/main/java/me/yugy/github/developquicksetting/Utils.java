package me.yugy.github.developquicksetting;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    public static void toast(Context context, @StringRes int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

    public static String getApkInstallPath(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isAppInstallInData(Context context) {
        String sourceDir = getApkInstallPath(context);
        log("App is install in: " + sourceDir);
        return sourceDir.startsWith("/data/");
    }

    public static void log(String log) {
        if (Conf.LOG_DEBUG_INFO_ENABLED) {
            Log.d(Conf.DEBUG_INFO_TAG, log);
        }
    }
}
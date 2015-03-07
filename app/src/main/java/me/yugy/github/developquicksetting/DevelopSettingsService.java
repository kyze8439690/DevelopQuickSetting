package me.yugy.github.developquicksetting;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;

public class DevelopSettingsService extends IntentService {

    @IntDef({ACTION_SET_SHOW_LAYOUT_BORDER, ACTION_SET_DISPLAY_OVERDRAW,
            ACTION_SET_PROFILE_GPU_RENDERING, ACTION_SET_IMMEDIATELY_DESTROY_ACTIVITIES})
    public @interface Action {}

    public static final int ACTION_SET_SHOW_LAYOUT_BORDER = 1;
    public static final int ACTION_SET_DISPLAY_OVERDRAW = 2;
    public static final int ACTION_SET_PROFILE_GPU_RENDERING = 3;
    public static final int ACTION_SET_IMMEDIATELY_DESTROY_ACTIVITIES = 4;

    public static void newTask(Context context, @Action int action) {
        context.startService(getIntent(context, action));
    }

    public static Intent getIntent(Context context, @Action int action) {
        Intent intent = new Intent(context, DevelopSettingsService.class);
        intent.putExtra("action", action);
        return intent;
    }

    public static PendingIntent getPendingIntent(Context context, @Action int action) {
        Intent intent = getIntent(context, action);
        return PendingIntent.getService(context, action, intent, 0);
    }

    public DevelopSettingsService() {
        super("WidgetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            int action = intent.getIntExtra("action", 0);
            Utils.log("onHandleIntent(), action: " + action);
            switch (action) {
                case ACTION_SET_SHOW_LAYOUT_BORDER:
                    refreshUIState(DeveloperSettings.toggleDebugLayout());
                    break;
                case ACTION_SET_DISPLAY_OVERDRAW:
                    refreshUIState(DeveloperSettings.toggleShowOverdraw());
                    break;
                case ACTION_SET_PROFILE_GPU_RENDERING:
                    refreshUIState(DeveloperSettings.toggleProfileGPURendering());
                    break;
                case ACTION_SET_IMMEDIATELY_DESTROY_ACTIVITIES:
                    refreshUIState(DeveloperSettings.toggleImmediatelyDestroyActivity(this));
                    break;
            }
        } catch (IOException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
            refreshUIState(false);
        }
    }

    private void refreshUIState(boolean success) {
        Utils.log("refreshUIState");
        //refresh widget state if exists.
        Intent intent = new Intent(this, DevelopWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] appWidgetIds = AppWidgetManager.getInstance(this).getAppWidgetIds(
                new ComponentName(this, DevelopWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(intent);

        //refresh activity state if exists.
        intent = new Intent(Conf.ACTION_REFRESH_UI);
        intent.putExtra("result", success);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

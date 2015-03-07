package me.yugy.github.developquicksetting;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;

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
                    if (DeveloperSettings.toggleDebugLayout()) {
                        refreshWidgetState();
                    }
                    break;
                case ACTION_SET_DISPLAY_OVERDRAW:
                    if (DeveloperSettings.toggleShowOverdraw()) {
                        refreshWidgetState();
                    }
                    break;
                case ACTION_SET_PROFILE_GPU_RENDERING:
                    if (DeveloperSettings.toggleProfileGPURendering()) {
                        refreshWidgetState();
                    }
                    break;
                case ACTION_SET_IMMEDIATELY_DESTROY_ACTIVITIES:
                    if (DeveloperSettings.toggleImmediatelyDestroyActivity(this)) {
                        refreshWidgetState();
                    }
                    break;
            }
        } catch (IOException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void refreshWidgetState() {
        Utils.log("refreshWidgetState");
        //refresh widget state if exists.
        Intent intent = new Intent(this, DevelopWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] appWidgetIds = AppWidgetManager.getInstance(this).getAppWidgetIds(
                new ComponentName(this, DevelopWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(intent);
    }
}

package me.yugy.github.developquicksetting;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;

import java.io.IOException;

public class DevelopWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Utils.log("onUpdate");
        int enabledColor = context.getResources().getColor(android.R.color.white);
        int disabledColor = context.getResources().getColor(android.R.color.tab_indicator_text);
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
            try {
                //update widget state
                views.setTextColor(R.id.layout_border,
                        DeveloperSettings.isDebugLayoutEnabled() ? enabledColor : disabledColor);
                views.setTextColor(R.id.overdraw,
                        DeveloperSettings.isShowOverdrawEnabled() ? enabledColor : disabledColor);
                views.setTextColor(R.id.gpu_rendering,
                        DeveloperSettings.isShowProfileGPURendering() ? enabledColor : disabledColor);
                views.setTextColor(R.id.destroy_activities,
                        DeveloperSettings.isImmediatelyDestroyActivities(context) ? enabledColor : disabledColor);

                //set widget click listener
                views.setOnClickPendingIntent(R.id.layout_border, DevelopSettingsService.getPendingIntent(
                        context, DevelopSettingsService.ACTION_SET_SHOW_LAYOUT_BORDER));
                views.setOnClickPendingIntent(R.id.overdraw, DevelopSettingsService.getPendingIntent(
                        context, DevelopSettingsService.ACTION_SET_DISPLAY_OVERDRAW));
                views.setOnClickPendingIntent(R.id.gpu_rendering, DevelopSettingsService.getPendingIntent(
                        context, DevelopSettingsService.ACTION_SET_PROFILE_GPU_RENDERING));
                views.setOnClickPendingIntent(R.id.destroy_activities, DevelopSettingsService.getPendingIntent(
                        context, DevelopSettingsService.ACTION_SET_IMMEDIATELY_DESTROY_ACTIVITIES));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //hide the button that device api level not supported.
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                views.setViewVisibility(R.id.gpu_rendering, View.GONE);
            }

            //hide the progressbar and show button
            views.setViewVisibility(R.id.loading, View.GONE);
            views.setViewVisibility(R.id.button_container, View.VISIBLE);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}

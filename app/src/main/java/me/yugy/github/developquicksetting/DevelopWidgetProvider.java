package me.yugy.github.developquicksetting;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import java.io.IOException;

public class DevelopWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Utils.log("onUpdate");
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
            try {
                //update widget state
                if (DeveloperSettings.isDebugLayoutEnabled()) {
                    views.setImageViewResource(R.id.layout_border_image, R.drawable.ic_debug_layout_enabled);
                    views.setImageViewResource(R.id.layout_border_indicator, R.color.appwidget_indicator_enabled);
                } else {
                    views.setImageViewResource(R.id.layout_border_image, R.drawable.ic_debug_layout_disabled);
                    views.setImageViewResource(R.id.layout_border_indicator, R.color.appwidget_indicator_disabled);
                }

                if (DeveloperSettings.isShowOverdrawEnabled()) {
                    views.setImageViewResource(R.id.overdraw_image, R.drawable.ic_overdraw_enabled);
                    views.setImageViewResource(R.id.overdraw_indicator, R.color.appwidget_indicator_enabled);
                } else {
                    views.setImageViewResource(R.id.overdraw_image, R.drawable.ic_overdraw_disabled);
                    views.setImageViewResource(R.id.overdraw_indicator, R.color.appwidget_indicator_disabled);
                }

                if (DeveloperSettings.isShowProfileGPURendering()) {
                    views.setImageViewResource(R.id.gpu_rendering_image, R.drawable.ic_gpu_rendering_enabled);
                    views.setImageViewResource(R.id.gpu_rendering_indicator, R.color.appwidget_indicator_enabled);
                } else {
                    views.setImageViewResource(R.id.gpu_rendering_image, R.drawable.ic_gpu_rendering_disabled);
                    views.setImageViewResource(R.id.gpu_rendering_indicator, R.color.appwidget_indicator_disabled);
                }

                if (DeveloperSettings.isImmediatelyDestroyActivities(context)) {
                    views.setImageViewResource(R.id.destroy_activities_image, R.drawable.ic_destroy_enabled);
                    views.setImageViewResource(R.id.destroy_activities_indicator, R.color.appwidget_indicator_enabled);
                } else {
                    views.setImageViewResource(R.id.destroy_activities_image, R.drawable.ic_destroy_disabled);
                    views.setImageViewResource(R.id.destroy_activities_indicator, R.color.appwidget_indicator_disabled);
                }

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

package me.yugy.github.developquicksetting;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import java.io.IOException;

public class DevelopWidgetProvider extends AppWidgetProvider {


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Utils.log("onAppWidgetOptionsChanged");
        refreshWidgets(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Utils.log("onUpdate");
        refreshWidgets(context, appWidgetManager, appWidgetIds);
    }

    private void refreshWidgets(Context context, AppWidgetManager appWidgetManager, int... appWidgetIds) {
        new RefreshWidgetsTask(context, appWidgetManager, appWidgetIds).execute();
    }


    private static class RefreshWidgetsTask extends AsyncTask<Void, Void, boolean[]> {

        private final Context mContext;
        private final int[] mAppWidgetIds;
        private final AppWidgetManager mAppWidgetManager;

        public RefreshWidgetsTask(Context context, AppWidgetManager appWidgetManager, int... appWidgetIds) {
            mContext = context;
            mAppWidgetManager = appWidgetManager;
            mAppWidgetIds = appWidgetIds;
        }

        @Override
        protected boolean[] doInBackground(Void... params) {
            try {
                return new boolean[] {
                        DeveloperSettings.isDebugLayoutEnabled(),
                        DeveloperSettings.isShowOverdrawEnabled(),
                        DeveloperSettings.isShowProfileGPURendering(),
                        DeveloperSettings.isImmediatelyDestroyActivities(mContext),
                        DeveloperSettings.isAdbThroughWifiEnabled()
                };
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(boolean[] results) {
            for (int appWidgetId : mAppWidgetIds) {
                RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.appwidget);
                //update widget state
                views.setImageViewResource(R.id.layout_border_image,
                        results[0] ? R.drawable.ic_debug_layout_enabled : R.drawable.ic_debug_layout_disabled);
                views.setImageViewResource(R.id.layout_border_indicator,
                        results[0] ? R.color.appwidget_indicator_enabled : R.color.appwidget_indicator_disabled);

                views.setImageViewResource(R.id.overdraw_image,
                        results[1] ? R.drawable.ic_overdraw_enabled : R.drawable.ic_overdraw_disabled);
                views.setImageViewResource(R.id.overdraw_indicator,
                        results[1] ? R.color.appwidget_indicator_enabled : R.color.appwidget_indicator_disabled);

                views.setImageViewResource(R.id.gpu_rendering_image,
                        results[2] ? R.drawable.ic_gpu_rendering_enabled : R.drawable.ic_gpu_rendering_disabled);
                views.setImageViewResource(R.id.gpu_rendering_indicator,
                        results[2] ? R.color.appwidget_indicator_enabled : R.color.appwidget_indicator_disabled);

                views.setImageViewResource(R.id.destroy_activities_image,
                        results[3] ? R.drawable.ic_destroy_enabled : R.drawable.ic_destroy_disabled);
                views.setImageViewResource(R.id.destroy_activities_indicator,
                        results[3] ? R.color.appwidget_indicator_enabled : R.color.appwidget_indicator_disabled);

                views.setImageViewResource(R.id.adb_wifi_image,
                        results[4] ? R.drawable.ic_adb_wifi_enabled : R.drawable.ic_adb_wifi_disabled);
                views.setImageViewResource(R.id.adb_wifi_indicator,
                        results[4] ? R.color.appwidget_indicator_enabled : R.color.appwidget_indicator_disabled);

                //set widget click listener
                views.setOnClickPendingIntent(R.id.layout_border, DevelopSettingsService.getPendingIntent(
                        mContext, DevelopSettingsService.ACTION_SET_SHOW_LAYOUT_BORDER));
                views.setOnClickPendingIntent(R.id.overdraw, DevelopSettingsService.getPendingIntent(
                        mContext, DevelopSettingsService.ACTION_SET_DISPLAY_OVERDRAW));
                views.setOnClickPendingIntent(R.id.gpu_rendering, DevelopSettingsService.getPendingIntent(
                        mContext, DevelopSettingsService.ACTION_SET_PROFILE_GPU_RENDERING));
                views.setOnClickPendingIntent(R.id.destroy_activities, DevelopSettingsService.getPendingIntent(
                        mContext, DevelopSettingsService.ACTION_SET_IMMEDIATELY_DESTROY_ACTIVITIES));
                views.setOnClickPendingIntent(R.id.adb_wifi, DevelopSettingsService.getPendingIntent(
                        mContext, DevelopSettingsService.ACTION_SET_ADB_THROUGH_WIFI));

                //hide the button that device api level not supported.
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    views.setViewVisibility(R.id.gpu_rendering, View.GONE);
                }

                //hide the progressbar and show button
                views.setViewVisibility(R.id.loading, View.GONE);
                views.setViewVisibility(R.id.button_container, View.VISIBLE);

                mAppWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }
}

package me.yugy.github.developquicksetting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.chainfire.libsuperuser.Shell;

public class InstallerActivity extends ActionBarActivity {

    public static void launch(Context context) {
        Intent intent = new Intent(context, InstallerActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        EasyTracker.getInstance(this).activityStart(this);
        setContentView(R.layout.activity_installer);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.install_and_reboot)
    void installAndReboot() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(getString(R.string.require_root_permission));
        dialog.show();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                String sourcePath = Utils.getApkInstallPath(InstallerActivity.this);
                String targetPath = getTargetFilePath();
                List<String> commands = new ArrayList<>();
                commands.add("mount -o rw,remount /system");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    commands.add("mkdir -p /system/priv-app/DevelopQuickSetting");
                }
                commands.add("cat " + sourcePath + " > " + targetPath);     //On some device, 'mv' is not allowed to copy file from /data to /system, use 'cat' instead
                commands.add("chmod 644 " + targetPath);
                commands.add("pkill zygote");
                Shell.SU.run(commands);

                //if the code below can be run, means that device do not have the 'pkill' command, try to hard reset.
                Shell.SU.run("reboot");
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //if the code below can be run, means that device run 'su reboot' failed, cause user have to reboot device manually.
                dialog.dismiss();
                new AlertDialog.Builder(InstallerActivity.this)
                        .setCancelable(true)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        })
                        .setMessage(R.string.install_exit_info)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Crashlytics.log(Behaviour.INSTALL_REBOOT_FAILED);
                                EasyTracker tracker = EasyTracker.getInstance(InstallerActivity.this);
                                tracker.set(Fields.customMetric(6), "1");
                                tracker.send(MapBuilder.createAppView().build());
                            }
                        })
                        .show();
            }
        }.execute();
    }

    private String getTargetFilePath() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return "/system/priv-app/DevelopQuickSetting/DevelopQuickSetting.apk";
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return "/system/priv-app/DevelopQuickSetting.apk";
        }
        return "/system/app/DevelopQuickSetting.apk";
    }

}

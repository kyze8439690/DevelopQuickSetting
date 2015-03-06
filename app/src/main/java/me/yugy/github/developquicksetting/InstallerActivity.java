package me.yugy.github.developquicksetting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import java.io.DataOutputStream;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class InstallerActivity extends ActionBarActivity {

    public static void launch(Context context) {
        Intent intent = new Intent(context, InstallerActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installer);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.install_and_reboot)
    void installAndReboot() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(getString(R.string.require_root_permission));
        dialog.show();

        try {
            String sourcePath = Utils.getApkInstallPath(this);
            String targetPath = getTargetFilePath();
            //get root permission
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream output = new DataOutputStream(process.getOutputStream());
            //mount /system
            output.writeBytes("mount -o rw,remount /system\n");
            //move the apk file to /system/app or /system/priv-app
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //make dir
                output.writeBytes("mkdir -p /system/priv-app/DevelopQuickSetting\n");
            }
            output.writeBytes("cat " + sourcePath + " > " + targetPath + "\n"); //On some device, 'mv' is not allowed to copy file from /data to /system, use 'cat' instead
            //change the file permission to 644
            output.writeBytes("chmod 644 " + targetPath + "\n");
            //soft reboot device
            output.writeBytes("pkill zygote\n");
            //exit su
            output.writeBytes("exit\n");
            output.flush();
            process.waitFor();
            output.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream output = new DataOutputStream(process.getOutputStream());
            //reboot
            output.writeBytes("reboot\n");
            //exit su
            output.writeBytes("exit\n");
            output.flush();
            process.waitFor();
            output.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        //if the code below can be run, means that device do not have the 'pkill' and 'su reboot' failed, cause user have to reboot device manually.
        dialog.dismiss();
        AlertDialog exitDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .setMessage(R.string.exit_info)
                .create();
        exitDialog.show();
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

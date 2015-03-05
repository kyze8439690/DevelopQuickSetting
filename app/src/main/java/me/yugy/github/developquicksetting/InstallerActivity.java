package me.yugy.github.developquicksetting;

import android.app.ProgressDialog;
import android.content.Context;
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
            output.writeBytes("mv -f " + sourcePath + " " + targetPath + "\n");
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
    }

    private String getTargetFilePath() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return "/system/priv-app/DevelopQuickSetting/DevelopQuickSetting.apk";
        }
        return "/system/app/" + getPackageName() + ".apk";
    }

}

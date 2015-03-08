package me.yugy.github.developquicksetting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity {

    private MainFragment mFragment;
    @InjectView(R.id.snackbar) TextView mSnackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (mFragment == null) {
            mFragment = new MainFragment();
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, mFragment).commit();
        }

        //check install path
        if (Utils.isAppInstallInData(this)) {
            InstallerActivity.launch(this);
            finish();
        }
    }

    public void showSnackBar(@StringRes int resId) {
        showSnackBar(getString(resId));
    }

    public void showSnackBar(String str) {
        mSnackBar.setVisibility(View.VISIBLE);
        mSnackBar.clearAnimation();
        mSnackBar.removeCallbacks(mHideRunnable);
        Animation showAnimation = AnimationUtils.loadAnimation(this, R.anim.show_snackbar);
        showAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mSnackBar.postDelayed(mHideRunnable, Conf.SNACKBAR_SHOW_DURATION);
            }

        });
        mSnackBar.setText(str);
        mSnackBar.startAnimation(showAnimation);
    }

    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            Animation hideAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.hide_snackbar);
            mSnackBar.startAnimation(hideAnimation);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.adb);
        if (item != null) {
            menu.removeItem(R.id.adb);      //remove

            SwitchCompat adbSwitch = (SwitchCompat) item.getActionView().findViewById(R.id.adb_switch);
            int isAdbChecked = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0);
            adbSwitch.setChecked(isAdbChecked == 1);
            if (mFragment != null) {
                mFragment.updatePreferencesState();
            }
            adbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Settings.Global.putInt(getContentResolver(), Settings.Global.ADB_ENABLED, isChecked ? 1 : 0);
                    if (mFragment != null) {
                        mFragment.updatePreferencesState();
                    }
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_developer_settings:
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(intent, 0);
                if (resolveInfoList.size() > 0) {
                    startActivity(intent);
                } else {
                    showSnackBar(R.string.can_not_open_developer_setting);
                }
                return true;
            case R.id.uninstall:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.uninstall_info)
                        .setTitle(R.string.warning)
                        .setPositiveButton(R.string.uninstall_and_reboot, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                uninstall();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void uninstall() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        try {
            String apkPath = Utils.getApkInstallPath(this);
            //get root permission
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream output = new DataOutputStream(process.getOutputStream());
            //mount /system
            output.writeBytes("mount -o rw,remount /system\n");
            //delete apk file
            output.writeBytes("rm " + apkPath + "\n");
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

        //if the code below can be run, means that device do not have the 'pkill' command, try to hard reset.
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

        //if the code below can be run, means that device run 'su reboot' failed, cause user have to reboot device manually.
        dialog.dismiss();
        AlertDialog exitDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .setMessage(R.string.uninstall_exit_info)
                .create();
        exitDialog.show();
    }
}

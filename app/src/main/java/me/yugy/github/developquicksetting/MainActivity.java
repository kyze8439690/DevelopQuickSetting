package me.yugy.github.developquicksetting;

import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener {

    @InjectView(R.id.adb) CheckBox mAdbCheckBox;
    @InjectView(R.id.layout_border) CheckBox mLayoutBorderCheckBox;
    @InjectView(R.id.overdraw) CheckBox mOverdrawCheckBox;
    @InjectView(R.id.profile_gpu) CheckBox mProfileGPUCheckBox;
    @InjectView(R.id.always_destroy_activities) CheckBox mAlwaysDestroyActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //remove previous listener(for onResume() will be called when create and resume), prevent next update will trigger the listener, after state update, set the listener back.
        removeCheckBoxClickListener();
        updateCheckBoxState();
        setCheckBoxClickListener();
    }

    private void updateCheckBoxState() {
        try {
            //adb
            int isAdbChecked = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED);
            mAdbCheckBox.setChecked(isAdbChecked == 1);

            //debug layout
            Process process = Runtime.getRuntime().exec("getprop " + PropertyKey.DEBUG_LAYOUT_PROPERTY);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String result = builder.toString();
            mLayoutBorderCheckBox.setChecked("true".equals(result));

            //overdraw
            process = Runtime.getRuntime().exec("getprop " + PropertyKey.DEBUG_OVERDRAW_PROPERTY);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            result = builder.toString();
            mOverdrawCheckBox.setChecked(!"false".equals(result));

            //profile gpu rendering
            process = Runtime.getRuntime().exec("getprop " + PropertyKey.PROFILE_PROPERTY);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            result = builder.toString();
            mProfileGPUCheckBox.setChecked(!"false".equals(result));

            //always destroy activities
            int isAlwaysDestroyActivitiesChecked = Settings.Global.getInt(getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES);
            mAlwaysDestroyActivities.setChecked(isAlwaysDestroyActivitiesChecked == 1);
        } catch (Settings.SettingNotFoundException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.update_checkbox_state_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void setCheckBoxClickListener() {
        mAdbCheckBox.setOnCheckedChangeListener(this);
        mLayoutBorderCheckBox.setOnCheckedChangeListener(this);
        mOverdrawCheckBox.setOnCheckedChangeListener(this);
        mProfileGPUCheckBox.setOnCheckedChangeListener(this);
        mAlwaysDestroyActivities.setOnCheckedChangeListener(this);
    }

    private void removeCheckBoxClickListener() {
        mAdbCheckBox.setOnCheckedChangeListener(null);
        mLayoutBorderCheckBox.setOnCheckedChangeListener(null);
        mOverdrawCheckBox.setOnCheckedChangeListener(null);
        mProfileGPUCheckBox.setOnCheckedChangeListener(null);
        mAlwaysDestroyActivities.setOnCheckedChangeListener(null);
    }

    private void setOtherCheckboxEnabled(boolean enabled) {
        mLayoutBorderCheckBox.setEnabled(enabled);
        mOverdrawCheckBox.setEnabled(enabled);
        mProfileGPUCheckBox.setEnabled(enabled);
        mAlwaysDestroyActivities.setEnabled(enabled);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            if (buttonView.equals(mAdbCheckBox)) {
                //adb
                Settings.Global.putInt(getContentResolver(), Settings.Global.ADB_ENABLED, isChecked ? 1 : 0);
                setOtherCheckboxEnabled(isChecked);
            } else if (buttonView.equals(mLayoutBorderCheckBox)) {
                //debug.layout
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream output = new DataOutputStream(process.getOutputStream());
                output.writeBytes("setprop " + PropertyKey.DEBUG_LAYOUT_PROPERTY + " " + (isChecked ? "true" : "false") + "\n");
                output.writeBytes("exit\n");
                output.flush();
                process.waitFor();
                output.close();
                if (process.exitValue() == 0) {
                    Toast.makeText(this, R.string.set_property_success_prompt, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.set_property_failed, Toast.LENGTH_SHORT).show();
                }
            } else if (buttonView.equals(mOverdrawCheckBox)) {
                //overdraw
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream output = new DataOutputStream(process.getOutputStream());
                output.writeBytes("setprop " + PropertyKey.DEBUG_OVERDRAW_PROPERTY + " " + (isChecked ? "show" : "false") + "\n");
                output.writeBytes("exit\n");
                output.flush();
                process.waitFor();
                output.close();
                if (process.exitValue() == 0) {
                    Toast.makeText(this, R.string.set_property_success_prompt, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.set_property_failed, Toast.LENGTH_SHORT).show();
                }
            } else if (buttonView.equals(mProfileGPUCheckBox)) {
                //profile gpu rendering
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream output = new DataOutputStream(process.getOutputStream());
                output.writeBytes("setprop " + PropertyKey.PROFILE_PROPERTY + " " + (isChecked ? "visual_bars" : "false") + "\n");
                output.writeBytes("exit\n");
                output.flush();
                process.waitFor();
                output.close();
                if (process.exitValue() == 0) {
                    Toast.makeText(this, R.string.set_property_success_prompt, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.set_property_failed, Toast.LENGTH_SHORT).show();
                }
            } else if (buttonView.equals(mAlwaysDestroyActivities)) {
                //always destroy activities

                //this piece is not work, throw a InvocationTargetException on android 5.0
                // that says "Caused by: java.lang.SecurityException: Package android does not belong to 10076"

//                Class activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
//                Method getDefaultMethod = activityManagerNativeClass.getMethod("getDefault");
//                Object activityManagerNativeInstance = getDefaultMethod.invoke(null);
//                Method setAlwaysFinishMethod = activityManagerNativeClass.getMethod("setAlwaysFinish", boolean.class);
//                setAlwaysFinishMethod.invoke(activityManagerNativeInstance, isChecked);

                Settings.Global.putInt(getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, isChecked ? 1 : 0);
            }
        } catch (IOException | InterruptedException e ) {
            e.printStackTrace();
            Toast.makeText(this, R.string.set_property_failed, Toast.LENGTH_SHORT).show();
        }
    }

}

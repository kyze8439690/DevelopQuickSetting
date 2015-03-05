package me.yugy.github.developquicksetting;

import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
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

    private static final String TAG = MainActivity.class.getName();

    private Switch mAdbSwitch;
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
            if (mAdbSwitch != null) {
                int isAdbChecked = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED);
                mAdbSwitch.setChecked(isAdbChecked == 1);
                setOtherCheckboxEnabled(isAdbChecked != 0);
            }

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
        if (mAdbSwitch != null) {
            mAdbSwitch.setOnCheckedChangeListener(this);
        }
        mLayoutBorderCheckBox.setOnCheckedChangeListener(this);
        mOverdrawCheckBox.setOnCheckedChangeListener(this);
        mProfileGPUCheckBox.setOnCheckedChangeListener(this);
        mAlwaysDestroyActivities.setOnCheckedChangeListener(this);
    }

    private void removeCheckBoxClickListener() {
        if (mAdbSwitch != null) {
            mAdbSwitch.setOnCheckedChangeListener(null);
        }
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
            if (buttonView.equals(mAdbSwitch)) {
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
            pokeSystemProperties();
        } catch (IOException | InterruptedException e ) {
            e.printStackTrace();
            Toast.makeText(this, R.string.set_property_failed, Toast.LENGTH_SHORT).show();
        }
    }

    void pokeSystemProperties() {
        new SystemPropPoker().execute();
    }

    static class SystemPropPoker extends AsyncTask<Void, Void, Void> {

        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Void... params) {
            String[] services;
            try {
                Class serviceManagerClass = Class.forName("android.os.ServiceManager");
                Method listServicesMethod = serviceManagerClass.getMethod("listServices");
                services = (String[]) listServicesMethod.invoke(null);
                for (String service : services) {
                    Method checkServiceMethod = serviceManagerClass.getMethod("checkService", String.class);
                    IBinder obj = (IBinder) checkServiceMethod.invoke(null, service);
                    if (obj != null) {
                        Parcel data = Parcel.obtain();
                        final int SYSPROPS_TRANSACTION = ('_'<<24)|('S'<<16)|('P'<<8)|'R'; //copy from source code in android.os.IBinder.java
                        try {
                            obj.transact(SYSPROPS_TRANSACTION, data, null, 0);
                        } catch (RemoteException ignored) {
                        } catch (Exception e) {
                            Log.i(TAG, "Someone wrote a bad service '" + service
                                    + "' that doesn't like to be poked: " + e);
                        }
                        data.recycle();
                    }
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.adb);
        mAdbSwitch = (Switch) item.getActionView().findViewById(R.id.adb_switch);
        mAdbSwitch.setOnCheckedChangeListener(this);
        try {
            int isAdbChecked = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED);
            mAdbSwitch.setChecked(isAdbChecked == 1);
            setOtherCheckboxEnabled(isAdbChecked != 0);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
}

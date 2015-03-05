package me.yugy.github.developquicksetting;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

    private CheckBoxPreference mLayoutBorderPreference;
    private CheckBoxPreference mDisplayOverdrawPreference;
    private CheckBoxPreference mProfileGPURenderingPreference;
    private CheckBoxPreference mImmediatelyDestroyActivitiesPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main);
        findPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Remove previous listener(for onResume() will be called when create and resume),
        // prevent next update will trigger the listener, after state update, set the listener back.
        removePreferencesListener();
        updatePreferencesState();
        setPreferencesListener();
    }

    private void findPreferences() {
        mLayoutBorderPreference = (CheckBoxPreference) findPreference(getString(R.string.key_layout_border));
        mDisplayOverdrawPreference = (CheckBoxPreference) findPreference(getString(R.string.key_display_overdraw));
        mProfileGPURenderingPreference = (CheckBoxPreference) findPreference(getString(R.string.key_profile_gpu_rendering));
        mImmediatelyDestroyActivitiesPreference = (CheckBoxPreference) findPreference(getString(R.string.key_always_destroy_activities));
    }

    private void removePreferencesListener() {
        mLayoutBorderPreference.setOnPreferenceChangeListener(null);
        mDisplayOverdrawPreference.setOnPreferenceChangeListener(null);
        mProfileGPURenderingPreference.setOnPreferenceChangeListener(null);
        mImmediatelyDestroyActivitiesPreference.setOnPreferenceChangeListener(null);
    }

    private void setPreferencesListener() {
        mLayoutBorderPreference.setOnPreferenceChangeListener(this);
        mDisplayOverdrawPreference.setOnPreferenceChangeListener(this);
        mProfileGPURenderingPreference.setOnPreferenceChangeListener(this);
        mImmediatelyDestroyActivitiesPreference.setOnPreferenceChangeListener(this);
    }

    public void updatePreferencesState() {
        try {
            //adb
            int isAdbChecked = Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED);
            setOtherPreferencesEnabled(isAdbChecked != 0);

            //debug layout
            Process process = Runtime.getRuntime().exec("getprop " + PropertyKey.DEBUG_LAYOUT_PROPERTY);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String result = builder.toString();
            mLayoutBorderPreference.setChecked("true".equals(result));

            //overdraw
            process = Runtime.getRuntime().exec("getprop " + PropertyKey.DEBUG_OVERDRAW_PROPERTY);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            result = builder.toString();
            mDisplayOverdrawPreference.setChecked(!"false".equals(result));

            //profile gpu rendering
            process = Runtime.getRuntime().exec("getprop " + PropertyKey.PROFILE_PROPERTY);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            result = builder.toString();
            mProfileGPURenderingPreference.setChecked(!"false".equals(result));

            //always destroy activities
            int isAlwaysDestroyActivitiesChecked = Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES);
            mImmediatelyDestroyActivitiesPreference.setChecked(isAlwaysDestroyActivitiesChecked == 1);
        } catch (Settings.SettingNotFoundException | IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.update_checkbox_state_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void setOtherPreferencesEnabled(boolean enabled) {
        mLayoutBorderPreference.setEnabled(enabled);
        mDisplayOverdrawPreference.setEnabled(enabled);
        mProfileGPURenderingPreference.setEnabled(enabled);
        mImmediatelyDestroyActivitiesPreference.setEnabled(enabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value = (boolean) newValue;
        try {
            if (preference.equals(mLayoutBorderPreference)) {
                //debug.layout
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream output = new DataOutputStream(process.getOutputStream());
                output.writeBytes("setprop " + PropertyKey.DEBUG_LAYOUT_PROPERTY + " " + (value ? "true" : "false") + "\n");
                output.writeBytes("exit\n");
                output.flush();
                process.waitFor();
                output.close();
                if (process.exitValue() == 0) {
                    Toast.makeText(getActivity(), R.string.set_property_success_prompt, Toast.LENGTH_SHORT).show();
                    mLayoutBorderPreference.setChecked(value);
                } else {
                    Toast.makeText(getActivity(), R.string.set_property_failed, Toast.LENGTH_SHORT).show();
                }
            } else if (preference.equals(mDisplayOverdrawPreference)) {
                //overdraw
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream output = new DataOutputStream(process.getOutputStream());
                output.writeBytes("setprop " + PropertyKey.DEBUG_OVERDRAW_PROPERTY + " " + (value ? "show" : "false") + "\n");
                output.writeBytes("exit\n");
                output.flush();
                process.waitFor();
                output.close();
                if (process.exitValue() == 0) {
                    Toast.makeText(getActivity(), R.string.set_property_success_prompt, Toast.LENGTH_SHORT).show();
                    mDisplayOverdrawPreference.setChecked(value);
                } else {
                    Toast.makeText(getActivity(), R.string.set_property_failed, Toast.LENGTH_SHORT).show();
                }
            } else if (preference.equals(mProfileGPURenderingPreference)) {
                //profile gpu rendering
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream output = new DataOutputStream(process.getOutputStream());
                output.writeBytes("setprop " + PropertyKey.PROFILE_PROPERTY + " " + (value ? "visual_bars" : "false") + "\n");
                output.writeBytes("exit\n");
                output.flush();
                process.waitFor();
                output.close();
                if (process.exitValue() == 0) {
                    Toast.makeText(getActivity(), R.string.set_property_success_prompt, Toast.LENGTH_SHORT).show();
                    mProfileGPURenderingPreference.setChecked(value);
                } else {
                    Toast.makeText(getActivity(), R.string.set_property_failed, Toast.LENGTH_SHORT).show();
                }
            } else if (preference.equals(mImmediatelyDestroyActivitiesPreference)) {
                //always destroy activities

                //this piece is not work, throw a InvocationTargetException on android 5.0
                // that says "Caused by: java.lang.SecurityException: Package android does not belong to 10076"

//                Class activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
//                Method getDefaultMethod = activityManagerNativeClass.getMethod("getDefault");
//                Object activityManagerNativeInstance = getDefaultMethod.invoke(null);
//                Method setAlwaysFinishMethod = activityManagerNativeClass.getMethod("setAlwaysFinish", boolean.class);
//                setAlwaysFinishMethod.invoke(activityManagerNativeInstance, isChecked);

                if (Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, value ? 1 : 0)) {
                    Toast.makeText(getActivity(), R.string.set_property_success_prompt, Toast.LENGTH_SHORT).show();
                    mImmediatelyDestroyActivitiesPreference.setChecked(value);
                } else {
                    Toast.makeText(getActivity(), R.string.set_property_failed, Toast.LENGTH_SHORT).show();
                }

            }
            pokeSystemProperties();
        } catch (IOException | InterruptedException e ) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.set_property_failed, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    void pokeSystemProperties() {
        new SystemPropPoker().execute();
    }
}

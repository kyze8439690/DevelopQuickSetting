package me.yugy.github.developquicksetting;

import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.provider.Settings;

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
        setPreferencesVisibility();
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

    private void setPreferencesVisibility() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ((PreferenceCategory)findPreference(getString(R.string.preference_category_key))).removePreference(mProfileGPURenderingPreference);
        }
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
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
        try {
            //adb
            if (getActivity() != null) {
                int isAdbChecked = Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED, 0);
                setOtherPreferencesEnabled(isAdbChecked != 0);
            }

            //debug layout
            Process process = Runtime.getRuntime().exec("getprop " + Property.DEBUG_LAYOUT_PROPERTY);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String result = builder.toString();
            if (mLayoutBorderPreference != null) {
                mLayoutBorderPreference.setChecked("true".equals(result));
            }

            //overdraw
            process = Runtime.getRuntime().exec("getprop " + Property.getDebugOverdrawPropertyKey());
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            result = builder.toString();
            if (mDisplayOverdrawPreference != null) {
                mDisplayOverdrawPreference.setChecked(Property.getDebugOverdrawPropertyEnabledValue().equals(result));
            }

            //profile gpu rendering
            process = Runtime.getRuntime().exec("getprop " + Property.PROFILE_PROPERTY);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            result = builder.toString();
            if (mProfileGPURenderingPreference != null) {
                mProfileGPURenderingPreference.setChecked("visual_bars".equals(result));
            }

            //always destroy activities
            if (getActivity() != null) {
                int isAlwaysDestroyActivitiesChecked = Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);
                mImmediatelyDestroyActivitiesPreference.setChecked(isAlwaysDestroyActivitiesChecked == 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ((MainActivity)getActivity()).showSnackBar(R.string.update_checkbox_state_failed);
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
                output.writeBytes("setprop " + Property.DEBUG_LAYOUT_PROPERTY + " " + (value ? "true" : "false") + "\n");
                output.writeBytes("exit\n");
                output.flush();
                process.waitFor();
                output.close();
                if (process.exitValue() == 0) {
                    ((MainActivity)getActivity()).showSnackBar(R.string.set_property_success_prompt);
                    mLayoutBorderPreference.setChecked(value);
                } else {
                    ((MainActivity)getActivity()).showSnackBar(R.string.set_property_failed);
                }
            } else if (preference.equals(mDisplayOverdrawPreference)) {
                //overdraw
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream output = new DataOutputStream(process.getOutputStream());
                output.writeBytes("setprop " + Property.getDebugOverdrawPropertyKey() + " " + (value ? Property.getDebugOverdrawPropertyEnabledValue() : "false") + "\n");
                output.writeBytes("exit\n");
                output.flush();
                process.waitFor();
                output.close();
                if (process.exitValue() == 0) {
                    ((MainActivity)getActivity()).showSnackBar(R.string.set_property_success_prompt);
                    mDisplayOverdrawPreference.setChecked(value);
                } else {
                    ((MainActivity)getActivity()).showSnackBar(R.string.set_property_failed);
                }
            } else if (preference.equals(mProfileGPURenderingPreference)) {
                //profile gpu rendering
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream output = new DataOutputStream(process.getOutputStream());
                output.writeBytes("setprop " + Property.PROFILE_PROPERTY + " " + (value ? "visual_bars" : "false") + "\n");
                output.writeBytes("exit\n");
                output.flush();
                process.waitFor();
                output.close();
                if (process.exitValue() == 0) {
                    ((MainActivity)getActivity()).showSnackBar(R.string.set_property_success_prompt);
                    mProfileGPURenderingPreference.setChecked(value);
                } else {
                    ((MainActivity)getActivity()).showSnackBar(R.string.set_property_failed);
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
                    ((MainActivity)getActivity()).showSnackBar(R.string.set_property_success_prompt);
                    mImmediatelyDestroyActivitiesPreference.setChecked(value);
                } else {
                    ((MainActivity)getActivity()).showSnackBar(R.string.set_property_failed);
                }

            }
            pokeSystemProperties();
        } catch (IOException | InterruptedException e ) {
            e.printStackTrace();
            ((MainActivity)getActivity()).showSnackBar(R.string.set_property_failed);
        }
        return false;
    }

    void pokeSystemProperties() {
        new SystemPropPoker().execute();
    }
}

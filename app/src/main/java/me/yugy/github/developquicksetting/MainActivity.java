package me.yugy.github.developquicksetting;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;


public class MainActivity extends ActionBarActivity {

    private MainFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.adb);
        Switch adbSwitch = (Switch) item.getActionView().findViewById(R.id.adb_switch);
        try {
            int isAdbChecked = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED);
            adbSwitch.setChecked(isAdbChecked == 1);
            if (mFragment != null) {
                mFragment.updatePreferencesState();
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
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
        return true;
    }

}

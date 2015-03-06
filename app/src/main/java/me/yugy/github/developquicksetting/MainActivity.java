package me.yugy.github.developquicksetting;

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

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity {

    private MainFragment mFragment;
    @InjectView(R.id.snackbar) TextView mSnackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

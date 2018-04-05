package com.zkt.testtool;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.IntentCompat;
import android.view.View;

import com.zkt.testtool.util.PrefUtils;

/**
 * 描述：日志输出设置界面
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //根据保存的日志筛选级别设置ToolBar的颜色
        int mToolbarColor = PrefUtils.getLevel(this).getColor();
        findViewById(R.id.toolbar).setBackgroundColor(mToolbarColor);
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    navigateUpToFromChild(SettingsActivity.this,
                            IntentCompat.makeMainActivity(new ComponentName(SettingsActivity.this,
                                    LogsListActivity.class)));
                } else {
                    Intent intent = new Intent(SettingsActivity.this, LogsListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }
    }


    public static class SettingsFragment extends PreferenceFragment {

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            bindPreferenceSummaryToValue(findPreference(PrefUtils.PREFERENCE_KEY_LOG_FORMAT));
            bindPreferenceSummaryToValue(findPreference(PrefUtils.PREFERENCE_KEY_LOG_BUFFER));
            bindPreferenceSummaryToValue(findPreference(PrefUtils.PREFERENCE_KEY_TEXT_SIZE));
            bindPreferenceSummaryToValue(findPreference(PrefUtils.PREFERENCE_KEY_TEXT_FONT));
        }

        private static void bindPreferenceSummaryToValue(Preference preference) {
            if (preference == null) return; //The user is lower API level
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PrefUtils.sp(preference.getContext())
                            .getString(preference.getKey(), ""));
        }

        private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                if (preference instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(value.toString());

                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                } else {
                    preference.setSummary(value.toString());
                }
                return true;
            }
        };


    }
}

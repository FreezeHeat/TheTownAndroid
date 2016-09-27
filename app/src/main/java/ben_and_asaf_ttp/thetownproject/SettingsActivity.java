package ben_and_asaf_ttp.thetownproject;

import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.os.Bundle;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String SETTINGS_CLEAR_LOCAL_DATA_KEY = "clearLocalData";
    private static final String APP_USERNAME = "username";
    private static final String APP_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SETTINGS_CLEAR_LOCAL_DATA_KEY)) {
            if (sharedPreferences.getBoolean(SETTINGS_CLEAR_LOCAL_DATA_KEY, false) == true) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(APP_USERNAME, "");
                editor.putString(APP_PASSWORD, "");
                editor.commit();
            }
        }
    }
}

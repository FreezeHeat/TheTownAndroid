package ben_and_asaf_ttp.thetownproject;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceActivity;
import android.os.Bundle;

public class SettingsActivity extends PreferenceActivity{

    private static final String SETTINGS_CLEAR_LOCAL_DATA_KEY = "clearLocalData";
    private static final String APP_USERNAME = "username";
    private static final String APP_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.bgsmall);
        getListView().setBackgroundColor(Color.TRANSPARENT);
        getListView().setCacheColorHint(Color.TRANSPARENT);


        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onBackPressed() {
        SharedPreferences.Editor preferences = getPreferenceScreen().getSharedPreferences().edit();
        if( getPreferenceScreen().getSharedPreferences().getBoolean(SETTINGS_CLEAR_LOCAL_DATA_KEY, false) == true){
            preferences.putString(APP_USERNAME, "");
            preferences.putString(APP_PASSWORD, "");
            preferences.putBoolean(SETTINGS_CLEAR_LOCAL_DATA_KEY, false);
            preferences.commit();
        }
        finish();
    }
}

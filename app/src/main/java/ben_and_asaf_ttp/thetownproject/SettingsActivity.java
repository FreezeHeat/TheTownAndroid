package ben_and_asaf_ttp.thetownproject;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

/**
 * Created by user on 19/09/2016.
 */
public class SettingsActivity extends PreferenceActivity{
    private CheckBoxPreference checkbox;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);




    }
}

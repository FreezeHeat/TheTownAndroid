package ben_and_asaf_ttp.thetownproject;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.view.View;

/**
 * Created by user on 19/09/2016.
 */
public class SettingsActivity extends PreferenceActivity implements View.OnClickListener{
    private CheckBoxPreference checkbox;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        addPreferencesFromResource(R.layout.activity_preferences);



    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
           // case R.id.pref_btnBack:

                //break;
            default:
                break;
        }
    }
}

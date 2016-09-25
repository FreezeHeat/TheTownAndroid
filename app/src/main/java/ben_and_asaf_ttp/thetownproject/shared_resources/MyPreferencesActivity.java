package ben_and_asaf_ttp.thetownproject.shared_resources;

 /**
 * Created by user on 19/09/2016.
 */
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.View;

import ben_and_asaf_ttp.thetownproject.R;

public class MyPreferencesActivity extends PreferenceActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // case R.id.pref_btnBack:

            //break;
            default:
                break;
        }
    }
}
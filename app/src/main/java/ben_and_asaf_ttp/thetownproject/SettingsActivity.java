package ben_and_asaf_ttp.thetownproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String APP_PASSWORD = "password";
    private static final String APP_NEWPASSWORD = "newpassword";
    private static final String BG_VOLUME = "bgVolume";
    private static final String FX_VOLUME = "fxVolume";
    private SharedPreferences preferences;
    private String password;
    private String toastMsg;
    private boolean valid;
    private GameService mService;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.bgsmall);
        getListView().setBackgroundColor(Color.TRANSPARENT);
        getListView().setCacheColorHint(Color.TRANSPARENT);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.preferences);
        password = (preferences.getString(APP_PASSWORD, ""));
        final EditTextPreference editTextPass = (EditTextPreference)findPreference(APP_NEWPASSWORD);
        editTextPass.setText(password);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        switch (key) {
            case APP_NEWPASSWORD:
                final SharedPreferences.Editor editor = getPreferenceScreen().getSharedPreferences().edit();
                final String pass = preferences.getString(APP_NEWPASSWORD, "");
                valid = true;
                toastMsg = "";

                //System.out.print(pass.equals(password));

                if (((!pass.isEmpty()) && (!pass.equals(password)))) {
                    if (pass.length() < 6) {
                        toastMsg = getResources().getString(R.string.general_password_too_few_characters);
                        valid = false;
                    } else if (pass.length() > 30) {
                        toastMsg = getResources().getString(R.string.general_password_too_much_characters);
                        valid = false;
                    }
                } else {
                    toastMsg = getResources().getString(R.string.pref_password_empty_or_same);
                    valid = false;
                }

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if (valid) {
                            DataPacket dp = new DataPacket();
                            dp.setCommand(Commands.EDIT_PASSWORD);
                            dp.setMessage(pass);
                            if (GameService.isRunning) {
                                mService.sendPacket(dp);
                                password = pass;
                                toastMsg = getResources().getString(R.string.pref_password_changed);
                            } else {
                                toastMsg = getResources().getString(R.string.general_connection_problem);
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        Toast.makeText(SettingsActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        editor.putString(APP_PASSWORD, password);
                        editor.commit();
                    }
                }.execute();
                break;
            case BG_VOLUME: {
                final float vol = getPreferenceScreen().getSharedPreferences().getFloat(key, 1.0f);
                AudioBackground.getBg().setVolume(vol, vol);
                break;
            }
            case FX_VOLUME: {
                final float vol = getPreferenceScreen().getSharedPreferences().getFloat(key, 1.0f);
                break;
            }
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GameService.LocalBinder binder = (GameService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, GameService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

}

package ben_and_asaf_ttp.thetownproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static SharedPreferences myPrefs;
    private Account account = new Account();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        account.setUsername(myPrefs.getString("username", ""));

        if(account.getUsername() != null && !account.getUsername().equals("")){
            finish();
            Intent myIntent = new Intent(this, Login.class);
            startActivity(myIntent);
        }
    }

    public void login(View v)
    {
        EditText editUser = (EditText)findViewById(R.id.editUser);
        EditText editPassword = (EditText)findViewById(R.id.editPass);

        account.setUsername(editUser.getText().toString());
        account.setPassword(editPassword.getText().toString());

        SharedPreferences.Editor editor = myPrefs.edit();
            editor.putString("username", account.getUsername());
            editor.putString("password", account.getPassword());
        editor.commit();

        Intent myIntent = new Intent(this, Login.class);
        startActivity(myIntent);
    }
}

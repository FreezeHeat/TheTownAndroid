package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static SharedPreferences myPrefs;
    private Account account = new Account();
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        account.setUsername(myPrefs.getString("username", ""));
        buildExitDialog();

        if(account.getUsername() != null && !account.getUsername().equals("")){
            finish();
            Intent myIntent = new Intent(this, Login.class);
            startActivity(myIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.exitApp:
                builder.create().show();
                break;
        }
        return true;
    }

    public void buildExitDialog(){
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                MainActivity.this.finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                dialog.cancel();
            }
        });
    }

    @Override
    public void onBackPressed(){
        builder.create().show();
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

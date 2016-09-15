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
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static SharedPreferences myPrefs;
    private Account account = new Account();
    private AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        Button btnForgot = (Button) findViewById(R.id.btnForgot);
        Button btnOptions = (Button) findViewById(R.id.btnOptions);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnForgot.setOnClickListener(this);
        btnOptions.setOnClickListener(this);



        /*
        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        account.setUsername(myPrefs.getString("username", ""));
        buildExitDialog();

        if(account.getUsername() != null && !account.getUsername().equals("")){
            finish();
            Intent myIntent = new Intent(this, Login.class);
            startActivity(myIntent);
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exitApp:
                builder.create().show();
                break;
        }
        return true;
    }

    public void buildExitDialog() {
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                MainActivity.this.finish();
                ClientConnection.getConnection().exit();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
    }

    @Override
    public void onBackPressed() {
        buildExitDialog();
        builder.create().show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnLogin:
                Intent btnLogin = new Intent(MainActivity.this, Login.class);
                MainActivity.this.startActivity(btnLogin);
                break;

            case R.id.btnRegister:
                Intent btnRegister = new Intent(MainActivity.this, Register.class);
                MainActivity.this.startActivity(btnRegister);
                break;

            case R.id.btnForgot:
                // do your code
                break;

            case R.id.btnOptions:
                // do your code
                break;
            default:
                break;
        }

    }
}
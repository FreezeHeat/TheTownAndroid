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
import android.widget.Toast;

import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences myPrefs;
    private Player player;
    private AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLogin = (Button) findViewById(R.id.main_btnLogin);
        Button btnRegister = (Button) findViewById(R.id.main_btnRegister);
        Button btnForgot = (Button) findViewById(R.id.main_btnForgot);
        Button btnOptions = (Button) findViewById(R.id.main_btnOptions);
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnForgot.setOnClickListener(this);
        btnOptions.setOnClickListener(this);

        //check if user already exists
        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        player = new Player(myPrefs.getString("username", ""), myPrefs.getString("password", ""));

        //If user already exists, get right into lobby activity
        if(player.getUsername() != null && (!player.getUsername().equals("")) &&
                player.getPassword() != null && (!player.getPassword().equals(""))) {
            finish();
            Intent myIntent = new Intent(this, Lobby.class);
            startActivity(myIntent);
        }
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
                buildExitDialog();
                builder.create().show();
                break;

            //TODO: add case for OPTIONS, instead of the main window
        }
        return true;
    }

    public void buildExitDialog() {
        if(builder == null) {
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
    }

    @Override
    public void onBackPressed() {
        buildExitDialog();
        builder.create().show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.main_btnLogin:
                Intent btnLogin = new Intent(MainActivity.this, Login.class);
                MainActivity.this.startActivity(btnLogin);
                finish();
                break;

            case R.id.main_btnRegister:
                Intent btnRegister = new Intent(MainActivity.this, Register.class);
                MainActivity.this.startActivity(btnRegister);
                finish();
                break;

            case R.id.main_btnForgot:
                // TODO: actual something to happen
                Toast.makeText(this, "In development", Toast.LENGTH_SHORT).show();
                break;

            case R.id.main_btnOptions:
                // TODO: actual something to happen
                Toast.makeText(this, "TODO!", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
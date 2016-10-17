package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private TextView welcome;
    private AlertDialog.Builder builder;
    private static final int MENU_EXIT = -1;
    private EditText editUser;
    private EditText editPassword;
    private CheckBox checkBox;
    private Player player = null;
    private DataPacket dp;
    private SharedPreferences myPrefs;
    private GlobalResources globalResources;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUser = (EditText)findViewById(R.id.login_editUser);
        editPassword = (EditText)findViewById(R.id.login_editPass);
        checkBox = (CheckBox)findViewById(R.id.login_checkboxRemember);
        checkBox.setChecked(true);
        player = new Player("", "");
        dp = new DataPacket();
        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        globalResources = (GlobalResources)getApplication();

        Button btnSignIn = (Button) findViewById(R.id.login_btnSignIn);
        Button btnForgotPass = (Button) findViewById(R.id.login_btnForgot);
        Button btnOptions = (Button) findViewById(R.id.login_btnOptions);
        btnSignIn.setOnClickListener(this);
        btnForgotPass.setOnClickListener(this);
        btnOptions.setOnClickListener(this);
    }

    public void login()
    {
        this.player.setUsername(this.editUser.getText().toString());
        this.player.setPassword(this.editPassword.getText().toString());

        //check if strings are empty
        if(this.player.getUsername() != null && (!this.player.getUsername().equals("")) &&
            this.player.getPassword() != null && (!this.player.getPassword().equals(""))) {

            new AsyncTask<DataPacket, Void, DataPacket>() {

                @Override
                protected DataPacket doInBackground(DataPacket... params) {
                    //login to server
                    DataPacket dp = params[0];
                    dp.setCommand(Commands.LOGIN);
                    dp.setPlayer(player);
                    ClientConnection.getConnection().sendDataPacket(dp);
                    dp = ClientConnection.getConnection().receiveDataPacket();
                    return dp;
                }

                @Override
                protected void onPostExecute(DataPacket dataPacket) {
                    if(dataPacket != null) {
                        switch (dataPacket.getCommand()) {
                            case LOGIN:
                                Login.this.player = dataPacket.getPlayer();
                                globalResources.setPlayer(player);

                                //check if user checked the checkbox to remember details
                                if (checkBox.isChecked()) {
                                    SharedPreferences.Editor editor = myPrefs.edit();
                                    editor.putString("username", player.getUsername());
                                    editor.putString("password", player.getPassword());
                                    editor.apply();
                                } else {
                                    SharedPreferences.Editor editor = myPrefs.edit();
                                    editor.putString("username", "");
                                    editor.putString("password", "");
                                    editor.commit();
                                }
                                Login.this.finish();
                                Intent myIntent = new Intent(Login.this, Lobby.class);
                                startActivity(myIntent);
                                return;
                            case ALREADY_CONNECTED:
                                Toast.makeText(Login.this, getResources().getText(R.string.login_already_connected), Toast.LENGTH_SHORT).show();
                                break;
                            case WRONG_DETAILS:
                                Toast.makeText(Login.this, getResources().getText(R.string.login_wrong_details), Toast.LENGTH_SHORT).show();

                                //reset password and focus on the password component
                                editPassword.setText("");
                                editPassword.requestFocus();
                                break;
                        }
                    }else{
                        buildExitDialog();
                        builder.show();
                    }
                }
            }.execute(this.dp);
        }else{
            Toast.makeText(Login.this, getResources().getText(R.string.general_empty_details), Toast.LENGTH_SHORT).show();
        }
    }

    public void checkboxClicked(View v){
        boolean checked = ((CheckBox)v).isChecked();
    }

    public void buildExitDialog() {
        if(builder == null) {
            builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getText(R.string.general_connection_problem));
            builder.setCancelable(false);
            builder.setPositiveButton(getResources().getString(R.string.general_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
            Login.this.finish();
            ClientConnection.getConnection().closeSocket();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //if there's a username and password
        String username = myPrefs.getString("username", "");
        if(username != null && !username.equals("")){
            editUser.setText(username);
            editPassword.setText(myPrefs.getString("password",""));
        }else{
            editUser.setText("");
            editPassword.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btnSignIn:
                login();
                break;
            case R.id.login_btnOptions:
                Intent btnOptions = new Intent(Login.this,SettingsActivity.class);
                Login.this.startActivity(btnOptions);
                break;
            case R.id.login_btnForgot:
                Intent btnForgot = new Intent(Login.this,Forgotpass.class);
                Login.this.startActivity(btnForgot);
                finish();
                break;
            default:
                break;
        }
    }
}

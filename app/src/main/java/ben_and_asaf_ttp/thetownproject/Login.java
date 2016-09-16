package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private TextView welcome;
    private AlertDialog.Builder builder;
    private static final int MENU_EXIT = -1;
    private Player player = null;
    private DataPacket dp;
    private EditText editUser;
    private EditText editPassword;
    private CheckBox checkBox;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUser = (EditText)findViewById(R.id.login_editUser);
        editPassword = (EditText)findViewById(R.id.login_editPass);
        checkBox = (CheckBox)findViewById(R.id.login_checkboxRemember);
        checkBox.setChecked(true);
        player = new Player("", "");
        dp = new DataPacket();

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

        new AsyncTask<DataPacket, Void, DataPacket>(){

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
                switch(dataPacket.getCommand()){
                    case LOGIN:
                        Login.this.player = dataPacket.getPlayer();

                        //check if user checked the checkbox to remember details
                        if(checkBox.isChecked()){
                            SharedPreferences.Editor editor = MainActivity.myPrefs.edit();
                            editor.putString("username", player.getUsername());
                            editor.putString("password", player.getPassword());
                            editor.apply();
                        }else{
                            SharedPreferences.Editor editor = MainActivity.myPrefs.edit();
                            editor.putString("username", "");
                            editor.putString("password", "");
                            editor.apply();
                        }
                        Login.this.finish();
                        Intent myIntent = new Intent(Login.this, Lobby.class);
                        startActivity(myIntent);
                        return;
                    case ALREADY_CONNECTED:
                        Toast.makeText(Login.this, getResources().getText(R.string.login_already_connected),Toast.LENGTH_SHORT).show();
                        break;
                    case WRONG_DETAILS:
                        Toast.makeText(Login.this, getResources().getText(R.string.login_wrong_details),Toast.LENGTH_SHORT).show();
                        break;
                }

                //reset password and focus on the password component
                editPassword.setText("");
                editPassword.requestFocus();
            }
        }.execute(this.dp);
    }

    public void checkboxClicked(View v){
        boolean checked = ((CheckBox)v).isChecked();
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
                Toast.makeText(Login.this, "In development", Toast.LENGTH_SHORT).show();
                break;
            case R.id.login_btnForgot:
                Toast.makeText(Login.this, "TODO!", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}

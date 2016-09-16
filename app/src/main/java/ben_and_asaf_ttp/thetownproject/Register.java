package ben_and_asaf_ttp.thetownproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class Register extends AppCompatActivity implements View.OnClickListener{
    private SharedPreferences myPrefs;
    private Player player;
    private DataPacket dp;
    private EditText editUser;
    private EditText editPassword;
    private EditText editPassword2;
    private EditText editEmail;
    private TextView txtConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        player = new Player("", "");
        dp = new DataPacket();

        editUser = (EditText)findViewById(R.id.register_editUser);
        editPassword = (EditText)findViewById(R.id.register_editPass);
        editPassword2 = (EditText)findViewById(R.id.register_editRePass);
        editEmail = (EditText)findViewById(R.id.register_txtEmail);
        txtConfirm = (TextView)findViewById(R.id.register_txtEmailResponse);

        Button btnConfirm = (Button) findViewById(R.id.register_btnConfirm);
        Button btnClear = (Button) findViewById(R.id.register_btnClear);
        btnConfirm.setOnClickListener(this);
        btnClear.setOnClickListener(this);

        //text confirm - hide it
        txtConfirm.setVisibility(View.INVISIBLE);

        //for now disable email
        //TODO: Email (Somewhat complex - on the SERVER SIDE)
        editEmail.setVisibility(View.GONE);
    }

    public void register()
    {
        //Check if empty
        if((!editUser.getText().toString().equals("")) && ((!editPassword.getText().equals("")) ||
            (!editPassword2.getText().equals("")))){

            //check if passwords match, if so run register function
            if(editPassword.getText().toString().equals(editPassword2.getText().toString())){
                this.player.setUsername(this.editUser.getText().toString());
                this.player.setPassword(this.editPassword.getText().toString());

                new AsyncTask<DataPacket, Void, DataPacket>(){
                    @Override
                    protected DataPacket doInBackground(DataPacket... params) {

                        //register to server
                        DataPacket dp = params[0];
                        dp.setCommand(Commands.REGISTER);
                        dp.setPlayer(player);
                        ClientConnection.getConnection().sendDataPacket(dp);
                        dp = ClientConnection.getConnection().receiveDataPacket();
                        return dp;
                    }

                    @Override
                    protected void onPostExecute(DataPacket dataPacket) {
                        switch(dataPacket.getCommand()){
                            case REGISTER:
                                Register.this.player = dataPacket.getPlayer();

                                //Save the information
                                SharedPreferences.Editor editor = myPrefs.edit();
                                player.getUsername();
                                myPrefs.getString("username", "");
                                editor.putString("username", player.getUsername());
                                editor.putString("password", player.getPassword());
                                editor.apply();

                                //start lobby intent
                                Register.this.finish();
                                Intent myIntent = new Intent(Register.this, Lobby.class);
                                startActivity(myIntent);
                                return;
                            case ALREADY_EXISTS:
                                Toast.makeText(Register.this, getResources().getText(R.string.register_user_already_exists),Toast.LENGTH_SHORT).show();

                                //refocus on the user
                                editUser.requestFocus();
                                break;
                        }
                    }
                }.execute(this.dp);
            }else{
                Toast.makeText(Register.this, getResources().getText(R.string.register_passwords_do_not_match), Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(Register.this, getResources().getText(R.string.general_empty_details), Toast.LENGTH_SHORT).show();
        }
    }

    public void clearTextFromComponents(){
        this.editUser.setText("");
        this.editPassword.setText("");
        this.editPassword2.setText("");
        this.editEmail.setText("");
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
            case R.id.register_btnConfirm:
                register();
                break;
            case R.id.register_btnClear:
                clearTextFromComponents();
                break;
            default:
                break;
        }
    }
}

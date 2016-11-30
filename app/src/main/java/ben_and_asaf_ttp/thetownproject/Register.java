package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
import ben_and_asaf_ttp.thetownproject.shared_resources.Game;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class Register extends AppCompatActivity implements View.OnClickListener{
    private SharedPreferences myPrefs;
    private Player player;
    private DataPacket dp;
    private EditText editUser;
    private EditText editPassword;
    private EditText editPassword2;
    private AlertDialog.Builder builder;
    private GameService mService;
    private boolean mBound = false;

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

        Button btnConfirm = (Button) findViewById(R.id.register_btnConfirm);
        Button btnClear = (Button) findViewById(R.id.register_btnClear);
        btnConfirm.setOnClickListener(this);
        btnClear.setOnClickListener(this);
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

                //password checks
                if(this.player.getPassword().length() < 6){
                    Toast.makeText(Register.this, getResources().getText(R.string.general_password_too_few_characters), Toast.LENGTH_SHORT).show();
                    return;
                }else if(this.player.getPassword().length() > 30){
                    Toast.makeText(Register.this, getResources().getText(R.string.general_password_too_much_characters), Toast.LENGTH_SHORT).show();
                    return;
                }

                new AsyncTask<DataPacket, Void, DataPacket>(){
                    @Override
                    protected DataPacket doInBackground(DataPacket... params) {

                        //register to server
                        DataPacket dp = params[0];
                        dp.setCommand(Commands.REGISTER);
                        dp.setPlayer(player);

                        if(GameService.isRunning) {
                            mService.sendPacket(dp);
                            dp = mService.getPacket();
                        }else{
                            dp.setCommand(Commands.CONNECTION_ERROR);
                        }
                        return dp;
                    }

                    @Override
                    protected void onPostExecute(DataPacket dataPacket) {
                        if(dataPacket != null) {
                            switch (dataPacket.getCommand()) {
                                case REGISTER:
                                    Register.this.player = dataPacket.getPlayer();
                                    Register.this.player.setFriends(new ArrayList<Player>());
                                    ((GlobalResources)getApplication()).setPlayer(player);

                                    //Save the information
                                    SharedPreferences.Editor editor = myPrefs.edit();
                                    editor.putString("username", player.getUsername());
                                    editor.putString("password", player.getPassword());
                                    editor.commit();

                                    //start lobby intent
                                    Register.this.finish();
                                    Intent myIntent = new Intent(Register.this, Lobby.class);
                                    startActivity(myIntent);
                                    return;
                                case ALREADY_EXISTS:
                                    Toast.makeText(Register.this, getResources().getText(R.string.register_user_already_exists), Toast.LENGTH_SHORT).show();

                                    //refocus on the user
                                    editUser.requestFocus();
                                    break;
                                case CONNECTION_ERROR:
                                    Toast.makeText(Register.this, getResources().getText(R.string.general_connection_problem), Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }else{
                            buildExitDialog();
                            builder.show();
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

    public void buildExitDialog() {
        if(builder == null) {
            builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getText(R.string.general_connection_problem));
            builder.setCancelable(false);
            builder.setPositiveButton(getResources().getString(R.string.general_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
            Register.this.finish();
            ClientConnection.getConnection().closeSocket();
                }
            });
        }
    }

    public void clearTextFromComponents(){
        this.editUser.setText("");
        this.editPassword.setText("");
        this.editPassword2.setText("");
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
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

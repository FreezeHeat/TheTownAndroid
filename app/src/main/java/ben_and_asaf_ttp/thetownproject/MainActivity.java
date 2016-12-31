package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
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
        //Button btnForgot = (Button) findViewById(R.id.main_btnForgot);
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        //btnForgot.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        final GlobalResources gr = (GlobalResources)getApplication();
        gr.getOpenActivites().remove(this);
        if(gr.getOpenActivites().isEmpty()) {
            ClientConnection.getConnection().closeSocket();
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        ((GlobalResources)getApplication()).getOpenActivites().add(this);
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        MainActivity.this.finish();
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
//            case R.id.main_btnForgot:
//                Intent btnForgot = new Intent(MainActivity.this,Forgotpass.class);
//                MainActivity.this.startActivity(btnForgot);
//                finish();
//                break;
            default:
                break;
        }
    }
}
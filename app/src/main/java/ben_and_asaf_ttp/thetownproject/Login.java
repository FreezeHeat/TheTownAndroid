package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Login extends AppCompatActivity {
    private TextView welcome;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        welcome = (TextView)findViewById(R.id.txtWelcome);

        //Welcome with the username
        welcome.setText(R.string.txtWelcome + " " + MainActivity.myPrefs.getString("username", ""));
    }

    public void buildExitDialog(){
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                Login.this.finish();
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

    public void newGame(View v){
        Intent myIntent = new Intent(this, NewGame.class);
        startActivity(myIntent);
    }

    public void showGames(View v){
        Intent myIntent = new Intent(this, ShowGames.class);
        startActivity(myIntent);
    }

    public void exit(View v){
        onBackPressed();
    }
}

package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class Login extends AppCompatActivity {
    private TextView welcome;
    AlertDialog.Builder builder;
    static final int MENU_EXIT = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.exitApp:
                onBackPressed();
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
        buildExitDialog();
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

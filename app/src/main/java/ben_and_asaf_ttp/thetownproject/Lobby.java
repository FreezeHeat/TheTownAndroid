package ben_and_asaf_ttp.thetownproject;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import ben_and_asaf_ttp.thetownproject.shared_resources.Player;


public class Lobby extends AppCompatActivity implements View.OnClickListener{

    private Player player;
    String[] friendsListName = {"Ben","Asaf"};
    int images[];
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private TextView txtPlayer;
    private TextView txtWon;
    private TextView txtLose;
    private TextView txtRatio;
    private TextView txtKills;
    private TextView txtHeals;
    private TextView txtRating;
    private ArrayAdapter<String> mAdapter;
    private GlobalResources globalResources;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        globalResources = (GlobalResources)getApplication();
        player = globalResources.getPlayer();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, friendsListName));

        Button btnJoinGame = (Button)findViewById(R.id.lobby_btn_joinGame);
        Button btnOptions = (Button)findViewById(R.id.lobby_btn_options);
        btnJoinGame.setOnClickListener(this);
        btnOptions.setOnClickListener(this);

        //set the player's name as a greeting
        txtPlayer = (TextView) findViewById(R.id.lobby_txt_player);
        txtWon = (TextView)findViewById(R.id.lobby_txt_won);
        txtLose = (TextView)findViewById(R.id.lobby_txt_lost);
        txtRatio = (TextView)findViewById(R.id.lobby_txt_ratio);
        txtKills = (TextView)findViewById(R.id.lobby_txt_kills);
        txtHeals = (TextView)findViewById(R.id.lobby_txt_heals);
        txtRating = (TextView)findViewById(R.id.lobby_txt_rating);

        txtPlayer.setText(player.getUsername());
        txtWon.setText(getResources().getText(R.string.lobby_stats_won) + ": " + player.getStats().getWon());
        txtLose.setText(getResources().getText(R.string.lobby_stats_lost) + ": " + player.getStats().getLost());
        txtRatio.setText(getResources().getText(R.string.lobby_stats_ratio) + ": " + player.getStats().getWinLoseRatio());
        txtKills.setText(getResources().getText(R.string.lobby_stats_kills) + ": " + player.getStats().getKills());
        txtHeals.setText(getResources().getText(R.string.lobby_stats_heals) + ": " + player.getStats().getHeals());
        txtRating.setText(getResources().getText(R.string.lobby_stats_rating) + ": " + player.getStats().getRating());
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
           case R.id.lobby_btn_joinGame:

               break;
            case R.id.lobby_btn_options:

                break;
        }
    }
}

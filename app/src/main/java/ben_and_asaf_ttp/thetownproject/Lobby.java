package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.Executor;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
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
    private AlertDialog dialogHowManyPlayers;
    private ProgressDialog dialogProgress;
    private int numPlayers = -1;
    private GameService mService;
    private boolean mBound = false;

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

        //How many players dialog that handles the player's request
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getText(R.string.lobby_matchMaking_howManyPlayers_title));
        builder.setSingleChoiceItems(R.array.lobby_array_how_many_players, -1, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //set value according to the array (QuickJoin[0], 5, 8 or 10)
                if(which == 0){

                    //if it's quick-join, set default of 0 (which is quick-join in the server)
                    Lobby.this.numPlayers = 0;
                }else {
                    Lobby.this.numPlayers = Integer.decode(getResources().getStringArray(R.array.lobby_array_how_many_players)[which]);
                }
                Lobby.this.dialogHowManyPlayers.dismiss();
                Lobby.this.dialogProgress.show();
                new AsyncTask<Void, Void, DataPacket>(){

                    @Override
                    protected DataPacket doInBackground(Void... params) {
                        DataPacket dp = new DataPacket();
                        dp.setCommand(Commands.READY);
                        dp.setNumber(Lobby.this.numPlayers);
                        ClientConnection.getConnection().sendDataPacket(dp);
                        dp = ClientConnection.getConnection().receiveDataPacket();
                        return dp;
                    }

                    // this runs on UI thread
                    @Override
                    protected void onPostExecute(DataPacket dp) {
                        if(dp.getCommand() == Commands.OK) {
                            ((GlobalResources) getApplication()).setGame(dp.getGame());
                            if (((GlobalResources) getApplication()).getGame() != null) {
                                dialogProgress.dismiss();
                            }
                        }
                        Intent myIntent = new Intent(Lobby.this, GameActivity.class);
                        startActivity(myIntent);
                        Lobby.this.finish();
                    }
                }.execute();
            }
        });

        dialogHowManyPlayers = builder.create();

        //Add progress dialog when a player waits for a game to be found, he can also cancel this
        dialogProgress = new ProgressDialog(this);
        dialogProgress.setMessage(getResources().getString(R.string.lobby_matchMaking_progress_message));
        dialogProgress.setTitle(getResources().getString(R.string.lobby_matchMaking_progress_title));
        dialogProgress.setButton(DialogInterface.BUTTON_NEGATIVE,
            getResources().getText(R.string.lobby_matchMaking_cancel),
            new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((GlobalResources)((GlobalResources) getApplication())).setGame(null);
                    dialogProgress.dismiss();
                }
            });
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        stopService(new Intent(this, GameService.class));
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
    public void onBackPressed() {

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                DataPacket dp = new DataPacket();
                dp.setCommand(Commands.DISCONNECT);
                ClientConnection.getConnection().sendDataPacket(dp);
                return null;
            }
        }.execute();
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
           case R.id.lobby_btn_joinGame:
               this.dialogHowManyPlayers.show();
               break;
            case R.id.lobby_btn_options:
                Intent btnOptions = new Intent(Lobby.this,SettingsActivity.class);
                Lobby.this.startActivity(btnOptions);
                break;
            default:
                break;
        }
    }
}

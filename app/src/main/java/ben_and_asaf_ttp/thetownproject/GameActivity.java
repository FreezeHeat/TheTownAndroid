package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
import ben_and_asaf_ttp.thetownproject.shared_resources.Game;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class GameActivity extends AppCompatActivity {
    private GlobalResources globalResources;
    private Game game;
    private Player player;
    private AlertDialog.Builder builder;
    private GridView grid;
    private Executor executor;
    private GameLogic gameLogic;
    private GameService mService;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        grid = (GridView)findViewById(R.id.game_playerGrid);
        globalResources = (GlobalResources)getApplication();
        game = globalResources.getGame();
        player = globalResources.getPlayer();
        gameLogic = new GameLogic();
        MyPlayerAdapter myAdapter = new MyPlayerAdapter(this, R.layout.player_card, game.getPlayers());
        grid.setAdapter(myAdapter);
        registerForContextMenu(grid);
    }

    class GameLogic implements Runnable{

        @Override
        public void run() {

            //start game service
            DataPacket dp;

            while(true){
                dp = mService.getPacket();
                switch(dp.getCommand()){
                    case REFRESH_PLAYERS:

                        //refreshed players - who is alive and who's not

                        break;
                    case DAY:

                        //day cycle - change GUI and chat

                        break;

                    case NIGHT:

                        //night cycle - change GUI and chat

                        break;
                    case SNITCH:

                        //get player - get his role for the SNITCH

                        break;
                    case EXECUTE:

                        //player was executed - get datapacket with player who was killed

                        break;
                    case PLAYER_JOINED:

                        //get a player and players (alert about the player and set the players)

                        break;
                    case PLAYER_LEFT:

                        //get the player, alert about him/her

                        break;
                    case READY:

                        //game begun (30 seconds wait and starting DAY phase) - get players

                        break;
                    case WIN_CITIZENS:

                        //get refreshed players with stats and game history - check each one for this player

                        break;
                    case WIN_KILLERS:

                        //get refreshed players with stats and game history - check each one for this player

                        break;
                    case SERVER_SHUTDOWN:

                        //server shuts down...

                        break;
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, GameService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //run game logic
        executor.execute(gameLogic);
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

    class MyPlayerAdapter extends ArrayAdapter<Player>
    {

        public MyPlayerAdapter(Context context, int resource, List<Player> objects) {
            super(context, resource, objects);
        }

        // the method getView is in charge of creating a single line in the list
        // it receives the position (index) of the line to be created
        // the method populates the view with the data from the relevant object (according to the position)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Get the data item for this position
            Player user = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view

            // the mechanism recycles objects - so it creates them only the firs time
            // if created already - only update the data inside
            // ( when scrolling)
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.player_card, parent, false);
            }
            // Lookup view for data population

            //TODO: Add actual images
            ImageView imgviewPlayerImage = (ImageView) convertView.findViewById(R.id.playerCard_playerImage);
            TextView txtPlayerName = (TextView) convertView.findViewById(R.id.playerCard_playerUsername);
            Button btnPlayerAction = (Button) convertView.findViewById(R.id.playerCard_btn_action);

            //imgviewPlayerImage.setImageDrawable();
            txtPlayerName.setText(user.getUsername());

            return convertView;

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playercard_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_playercard_see_stats:
                Toast.makeText(this, ((Player)grid.getItemAtPosition(info.position)).getStats().toString(), Toast.LENGTH_SHORT);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    public void buildExitDialog() {
        if(builder == null) {
            builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getText(R.string.game_exitDialog));
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    DataPacket dp = new DataPacket();
                    dp.setCommand(Commands.DISCONNECT);
                    ClientConnection.getConnection().sendDataPacket(dp);
                    Intent myIntent = new Intent(GameActivity.this, Lobby.class);
                    startActivity(myIntent);
                    GameActivity.this.finish();
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
}

package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;
import java.util.concurrent.Executor;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
import ben_and_asaf_ttp.thetownproject.shared_resources.Game;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;
import ben_and_asaf_ttp.thetownproject.shared_resources.Roles;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{
    private GlobalResources globalResources;
    private Game game;
    private Player player;
    private AlertDialog.Builder builder;
    private GridView grid;
    private EditText txtSendMessage;
    private TextView txtGameChat;
    private TextView txtGameTimer;
    private ImageView imgvGamePhase;
    private Executor executor;
    private GameLogic gameLogic;
    private GameService mService;
    private boolean mBound = false;
    private MyPlayerAdapter myAdapter;
    private DataPacket out;
    private boolean day;
    private boolean gameStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        grid = (GridView)findViewById(R.id.game_playerGrid);
        Button btnSendMsg = (Button)findViewById(R.id.game_btn_send);
        btnSendMsg.setOnClickListener(this);
        txtSendMessage = (EditText)findViewById(R.id.game_txt_sendMessage);
        txtGameTimer = (TextView)findViewById(R.id.game_txt_timer);
        imgvGamePhase = (ImageView)findViewById(R.id.game_imgv_phase);
        txtGameChat = (TextView)findViewById(R.id.game_chat_txt);

        txtGameChat.setMovementMethod(new ScrollingMovementMethod());
        globalResources = (GlobalResources)getApplication();
        game = globalResources.getGame();
        player = globalResources.getPlayer();
        gameLogic = new GameLogic();
        myAdapter = new MyPlayerAdapter(this, R.layout.player_card, game.getPlayers());
        grid.setAdapter(myAdapter);
        registerForContextMenu(grid);
        out = new DataPacket();
        day = true;

        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        };
    }

    class GameLogic implements Runnable{

        @Override
        public void run() {

            //start game service
            DataPacket dp = null;
            String msg;

            while(true){
                dp = mService.getPacket();
                Intent myIntent;

                switch(dp.getCommand()){
                    case SEND_MESSAGE:
                    case SEND_MESSAGE_DEAD:
                    case SEND_MESSAGE_KILLER:
                        msg = dp.getMessage();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //TODO: add to chat with HTML code
                            }
                        });
                        break;
                    case REFRESH_PLAYERS:

                        //refreshed players - who is alive and who's not
                        game.getPlayers().clear();
                        game.getPlayers().addAll(dp.getPlayers());
                        player = game.getPlayers().get(game.getPlayers().indexOf(player));
                        game.getPlayers().remove(player);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyDataSetChanged();
                            }
                        });
                        break;
                    case DAY:

                        //day cycle - change GUI and chat
                        day = true;
                        msg = getResources().getString(R.string.game_day_phase);
                        if(GameActivity.this.player.isAlive())
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtSendMessage.setEnabled(true);
                            }
                        });
                        //TODO: Animation + chat alert + imageView + Timer update
                        break;
                    case NIGHT:

                        //night cycle - change GUI and chat
                        day = false;
                        msg = getResources().getString(R.string.game_day_phase);
                        if(GameActivity.this.player.isAlive()) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    txtSendMessage.setEnabled(false);
                                }
                            });
                        }
                        //TODO: Animation + chat alert + imageView + Timer update
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if(!(player.getRole() == Roles.KILLER)){
//                                    txtSendMessage.setEnabled(false);
//                                }
//                            }
//                        });
                        break;
                    case SNITCH:

                        //get player - get his role for the SNITCH
                        msg = String.format(
                                getResources().getString(R.string.game_snitch_identity).toString(),
                                dp.getPlayer());
                        Roles role = dp.getPlayer().getRole();
                        if(role == Roles.KILLER){
                            msg = msg.concat(" " + getResources().getString(R.string.game_role_killer));
                        }else if (role == Roles.HEALER){
                            msg = msg.concat(" " + getResources().getString(R.string.game_role_healer));
                        }else if (role == Roles.SNITCH){
                            msg = msg.concat(" " + getResources().getString(R.string.game_role_snitch));
                        }else{
                            msg = msg.concat(" " + getResources().getString(R.string.game_role_citizen));
                        }
                        //TODO: Animation + Dialog with snitched player + Chat alert
                        break;
                    case EXECUTE:

                        //player was executed - get datapacket with player who was killed
                        if (player.equals(dp.getPlayer())) {
                           msg = getResources().getString(R.string.game_executed_you);
                        } else {
                            msg = String.format(getResources().getString(R.string.game_executed_other), dp.getPlayer().getUsername());
                        }
                        //TODO: Animation + Dialog with executed player + Chat alert
                        break;
                    case PLAYER_JOINED:

                        //get a player and players (alert about the player and set the players)
                        msg = String.format(getResources().getString(R.string.game_player_joined), dp.getPlayer().getUsername());
                        game.getPlayers().add(dp.getPlayer());
                        game.getPlayers().remove(player);
                        //TODO: Chat alert + (Maybe, txtTimer will be used as player count?)
                        break;
                    case PLAYER_LEFT:

                        //get the player, alert about him/her
                        msg = String.format(getResources().getString(R.string.game_player_left), dp.getPlayer().getUsername());
                        game.getPlayers().remove(dp.getPlayer());
                        //TODO: Chat alert + (Maybe, txtTimer will be used as player count?)
                        break;
                    case READY:

                        //game begun (30 seconds wait and starting DAY phase) - get players
                        msg = getResources().getString(R.string.game_ready_phase);
                        game.getPlayers().clear();
                        game.getPlayers().addAll(dp.getPlayers());
                        player = game.getPlayers().get(game.getPlayers().indexOf(player));
                        game.getPlayers().remove(player);
                        gameStarted = true;
                        //TODO: Animation + Chat alert + Timer update
                        break;
                    case WIN_CITIZENS:

                        //get refreshed players with stats and game history - check each one for this player
                        msg = getResources().getString(R.string.game_ready_phase);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //TODO: Animation
                        myIntent = new Intent(GameActivity.this, Lobby.class);
                        startActivity(myIntent);
                        finish();
                        return;
                    case WIN_KILLERS:

                        //get refreshed players with stats and game history - check each one for this player
                        msg = getResources().getString(R.string.game_ready_phase);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //TODO: Animation
                        myIntent = new Intent(GameActivity.this, Lobby.class);
                        startActivity(myIntent);
                        finish();
                        return;
                    case GAME_DISBANDED:
                        msg = getResources().getString(R.string.game_game_disbanded);
                        //TODO: AlertDialog
                        myIntent = new Intent(GameActivity.this, Lobby.class);
                        startActivity(myIntent);
                        finish();
                        break;
                    case SERVER_SHUTDOWN:
                        buildConfirmDialog(getResources().getString(R.string.general_server_shutdown));
                        builder.show();
                        return;
                    default:
                        if(dp != null) {
                            Log.i(this.getClass().getName(), "DataPacket received: " + dp.toString());
                        }else{
                            Log.e(this.getClass().getName(), "DataPacket is null");
                        }
                        return;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.game_btn_send:
                if(player.isAlive() == true) {
                    if (day){
                        out.setCommand(Commands.SEND_MESSAGE);
                    }else if(player.getRole() == Roles.KILLER){
                        out.setCommand(Commands.SEND_MESSAGE_KILLER);
                    }
                }else{
                    out.setCommand(Commands.SEND_MESSAGE_DEAD);
                }
                out.setMessage(player.getUsername() + ": " + txtSendMessage.getText().toString());
                mService.sendPacket(out);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, GameService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {

        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
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

            //run game logic
            if(GameService.isRunning) {
                executor.execute(gameLogic);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    class MyPlayerAdapter extends ArrayAdapter<Player> implements View.OnCreateContextMenuListener
    {

        public MyPlayerAdapter(Context context, int resource, List<Player> objects){
            super(context, resource, objects);
        }

        // the method getView is in charge of creating a single line in the list
        // it receives the position (index) of the line to be created
        // the method populates the view with the data from the relevant object (according to the position)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Get the data item for this position
            final Player user = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            // the mechanism recycles objects - so it creates them only the firs time
            // if created already - only update the data inside
            // ( when scrolling)
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.player_card, parent, false);
            }

            final TextView txtPlayerName = (TextView) convertView.findViewById(R.id.playerCard_txt_playerUsername);
            final TextView txtPlayerStatus = (TextView) convertView.findViewById(R.id.playerCard_txt_playerStatus);
            final TextView btnPlayerAction = (Button) convertView.findViewById(R.id.playerCard_btn_action);
            final Roles role = GameActivity.this.player.getRole();

            txtPlayerName.setText(user.getUsername());

            if(user.isAlive()) {
                txtPlayerStatus.setText(getResources().getText(R.string.game_player_alive));
                if (role != null) {
                    if (role == Roles.KILLER) {
                        btnPlayerAction.setText(getResources().getText(R.string.game_kill));
                    } else if (role == Roles.HEALER) {
                        btnPlayerAction.setText(getResources().getText(R.string.game_heal));
                    } else if (role == Roles.SNITCH) {
                        btnPlayerAction.setText(getResources().getText(R.string.game_snitch));
                    } else {
                        btnPlayerAction.setText(getResources().getText(R.string.game_vote));
                    }
                }
                btnPlayerAction.setVisibility(View.VISIBLE);
            }else{
                txtPlayerStatus.setText(getResources().getText(R.string.game_player_dead));
                btnPlayerAction.setVisibility(View.INVISIBLE);
            }

            btnPlayerAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Check if the game started and user is alive or not as well as the user the
                    //action is set upon
                    if(GameActivity.this.gameStarted) {
                        if(GameActivity.this.player.isAlive()) {
                            if (user.isAlive()) {
                                if (!day) {
                                    GameActivity.this.player.getRole().action(GameActivity.this.out);
                                    GameActivity.this.out.setPlayer(player);
                                    GameActivity.this.mService.sendPacket(GameActivity.this.out);
                                } else {
                                    GameActivity.this.out.setCommand(Commands.VOTE);
                                    GameActivity.this.out.setPlayer(player);
                                    GameActivity.this.mService.sendPacket(GameActivity.this.out);
                                }
                            } else {
                                Toast.makeText(
                                        GameActivity.this,
                                        getResources().getText(R.string.game_action_upon_dead),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(
                                    GameActivity.this,
                                    getResources().getText(R.string.game_action_you_dead),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });
            return convertView;
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {}
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playercard_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_playercard_see_stats:
                ((GlobalResources)getApplication()).setStatsPlayer(((Player)grid.getItemAtPosition(info.position)));
                Intent intent = new Intent(GameActivity.this, StatsActivity.class);
                startActivity(intent);
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

            builder.setPositiveButton(getResources().getString(R.string.general_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    out.setCommand(Commands.DISCONNECT);
                    mService.sendPacket(out);
                    Intent myIntent = new Intent(GameActivity.this, Lobby.class);
                    startActivity(myIntent);
                    GameActivity.this.finish();
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.general_no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        }
    }

    public void buildConfirmDialog(String msg) {
        builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.general_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                GameActivity.this.finish();
                ClientConnection.getConnection().closeSocket();
            }
        });
    }

    @Override
    public void onBackPressed() {
        buildExitDialog();
        builder.show();
    }
}

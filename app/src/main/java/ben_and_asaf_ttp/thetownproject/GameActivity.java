package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
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

import static ben_and_asaf_ttp.thetownproject.shared_resources.Commands.NIGHT;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{
    private Game game;
    private Player player;
    private Player target;
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
    private CountDownTimer countDownTimer;
    private boolean day;
    private boolean gameStarted;
    private String msg;

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
        txtSendMessage.setMovementMethod(new ScrollingMovementMethod());

        game = ((GlobalResources)getApplication()).getGame();
        player = ((GlobalResources)getApplication()).getPlayer();
        gameLogic = new GameLogic();
        myAdapter = new MyPlayerAdapter(this, R.layout.player_card, game.getPlayers());
        grid.setAdapter(myAdapter);
        registerForContextMenu(grid);
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

            while(true){
                dp = mService.getPacket();
                Intent myIntent;

                switch(dp.getCommand()){
                    case SEND_MESSAGE:
                        msg = "<font color=\"#009933\">" +
                                dp.getPlayer().getUsername() +
                                ": </font><font color=\"#ffffff\">" +
                                dp.getMessage() +
                                "</font><br/>";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtGameChat.append(Html.fromHtml(msg));
                            }
                        });
                        break;
                    case SEND_MESSAGE_DEAD:
                        msg = "<font color=\"#cc0000\">*DEAD*</font><font color=\"#009933\">" +
                                dp.getPlayer().getUsername() +
                                ": </font><font color=\"#ffffff\">"
                                + dp.getMessage() +
                                "</font><br/>";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtGameChat.append(Html.fromHtml(msg));
                            }
                        });
                        break;
                    case SEND_MESSAGE_KILLER:
                        msg = "<font color=\"#ff3300\">" +
                                dp.getPlayer().getUsername() +
                                ": </font><font color=\"#ffffff\">" +
                                dp.getMessage() +
                                "</font><br/>";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtGameChat.append(Html.fromHtml(msg));
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
                        msg = "<font color=\"#0066ff\">*"+ getResources().getString(R.string.game_day_phase) + "*</font><br/>";
                        if(GameActivity.this.player.isAlive()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtSendMessage.setEnabled(true);
                                }
                            });
                        }

                        //TODO: Animation + imageView
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imgvGamePhase.setImageResource(R.drawable.day);
                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                countDownTimer = new CountDownTimer(60000, 1000) {
                                    @Override
                                    public void onTick(long millisRemaining) {
                                        txtGameTimer.setText(String.valueOf(millisRemaining / 1000));
                                    }

                                    @Override
                                    public void onFinish() {
                                        new AsyncTask<Void, Void, Void>() {
                                            @Override
                                            protected Void doInBackground(Void... voids) {
                                                DataPacket dp = new DataPacket();
                                                dp.setCommand(Commands.VOTE);
                                                dp.setPlayer(GameActivity.this.target);
                                                mService.sendPacket(dp);
                                                return null;
                                            }
                                        }.execute();
                                        GameActivity.this.target = null;
                                        txtGameTimer.setText("");
                                    }
                                }.start();
                            }
                        });
                        break;
                    case NIGHT:

                        //night cycle - change GUI and chat
                        day = false;
                        msg = "<font color=\"#0066ff\">*" + getResources().getString(R.string.game_night_phase) + "*</font><br/>";
                        if(GameActivity.this.player.isAlive()) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    txtSendMessage.setEnabled(false);
                                }
                            });
                        }
                        //TODO: Animation + imageView
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imgvGamePhase.setImageResource(R.drawable.night);
                            }
                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                countDownTimer = new CountDownTimer(30000, 1000) {
                                    @Override
                                    public void onTick(long millisRemaining) {
                                        txtGameTimer.setText(String.valueOf(millisRemaining / 1000));
                                    }

                                    @Override
                                    public void onFinish() {
                                        //TODO: send action
                                        new AsyncTask<Void, Void, Void>() {
                                            @Override
                                            protected Void doInBackground(Void... voids) {
                                                DataPacket dp = GameActivity.this.player.getRole().action(new DataPacket());
                                                dp.setPlayer(GameActivity.this.target);
                                                mService.sendPacket(dp);
                                                return null;
                                            }
                                        }.execute();
                                        GameActivity.this.target = null;
                                        txtGameTimer.setText("");
                                    }
                                }.start();
                            }
                        });
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
                                "<font color=\"#0066ff\">*" +
                                getResources().getString(R.string.game_snitch_identity),dp.getPlayer());
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
                        msg = msg.concat("*</font><br/>");
                        //TODO: Dialog with snitched player
                        break;
                    case EXECUTE:

                        //player was executed - get datapacket with player who was killed
                        msg = "<font color=\"#0066ff\">*";
                        if (player.equals(dp.getPlayer())) {
                           msg = msg.concat(getResources().getString(R.string.game_executed_you));
                        } else {
                            msg = msg.concat(String.format(
                                    getResources().getString(R.string.game_executed_other),
                                    dp.getPlayer().getUsername()));
                        }
                        msg = msg.concat("*</font><br/>");
                        //TODO: Animation + Dialog with executed player
                        break;
                    case PLAYER_JOINED:

                        //get a player and players (alert about the player and set the players)
                        msg = String.format(
                                "<font color=\"#0066ff\">*" +
                                getResources().getString(R.string.game_player_joined) +
                                "*</font><br/>", dp.getPlayer().getUsername());
                        game.getPlayers().add(dp.getPlayer());
                        game.getPlayers().remove(GameActivity.this.player);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtGameTimer.setText((game.getPlayers().size() + 1) + " / " + game.getMaxPlayers());
                                myAdapter.notifyDataSetChanged();
                            }
                        });
                        break;
                    case PLAYER_LEFT:

                        //get the player, alert about him/her
                        msg = String.format(
                                "<font color=\"#0066ff\">*" +
                                getResources().getString(R.string.game_player_left) +
                                "*</font><br/>", dp.getPlayer().getUsername());
                        game.getPlayers().remove(dp.getPlayer());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtGameTimer.setText((game.getPlayers().size() + 1) + " / " + game.getMaxPlayers());
                                myAdapter.notifyDataSetChanged();
                            }
                        });
                        break;
                    case READY:

                        //game begun (30 seconds wait and starting DAY phase) - get players
                        msg =  "<font color=\"#0066ff\">*" +
                                getResources().getString(R.string.game_ready_phase) +
                                "*</font><br/>";
                        game.getPlayers().clear();
                        game.getPlayers().addAll(dp.getPlayers());
                        player = game.getPlayers().get(game.getPlayers().indexOf(player));
                        game.getPlayers().remove(player);
                        gameStarted = true;
                        //TODO: Animation
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                countDownTimer = new CountDownTimer(30000, 1000) {
                                    @Override
                                    public void onTick(long millisRemaining) {
                                        txtGameTimer.setText(String.valueOf(millisRemaining / 1000));
                                    }

                                    @Override
                                    public void onFinish() {
                                        txtGameTimer.setText("");
                                    }
                                }.start();
                                myAdapter.notifyDataSetChanged();
                            }
                        });

                        break;
                    case WIN_CITIZENS:

                        //get refreshed players with stats and game history - check each one for this player
                        msg = getResources().getString(R.string.game_citizens_win);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //TODO: Dialog
                        myIntent = new Intent(GameActivity.this, Lobby.class);
                        startActivity(myIntent);
                        finish();
                        return;
                    case WIN_KILLERS:

                        //get refreshed players with stats and game history - check each one for this player
                        msg = getResources().getString(R.string.game_killers_win);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //TODO: Dialog
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
                if(!txtSendMessage.getText().toString().isEmpty()) {
                    final String string = txtSendMessage.getText().toString();
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            DataPacket dp = new DataPacket();

                            if (player.isAlive() == true) {
                                if (day) {
                                    dp.setCommand(Commands.SEND_MESSAGE);
                                } else if (player.getRole() == Roles.KILLER) {
                                    dp.setCommand(Commands.SEND_MESSAGE_KILLER);
                                }
                            } else {
                                dp.setCommand(Commands.SEND_MESSAGE_DEAD);
                            }
                            dp.setMessage(string);
                            mService.sendPacket(dp);
                            return null;
                        }
                    }.execute();
                    txtSendMessage.setText("");
                }
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
                                GameActivity.this.target = user;
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
                    DataPacket dp = new DataPacket();
                    dp.setCommand(Commands.DISCONNECT);
                    mService.sendPacket(dp);
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

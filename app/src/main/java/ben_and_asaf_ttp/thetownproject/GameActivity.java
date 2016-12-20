package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
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
    private Button btn_target;
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
    private boolean online;
    private String msg;
    private Intent anim;
    private Intent announce;
    private TextView playerRole;
    private DataPacket dp;

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
        playerRole = (TextView)findViewById(R.id.gamePlayerRole);
        txtGameChat.setMovementMethod(new ScrollingMovementMethod());
        txtSendMessage.setMovementMethod(new ScrollingMovementMethod());

        anim = new Intent(GameActivity.this, Pop.class);
        announce = new Intent(GameActivity.this, Announce.class);

        game = ((GlobalResources)getApplication()).getGame();
        player = ((GlobalResources)getApplication()).getPlayer();
        gameLogic = new GameLogic();
        myAdapter = new MyPlayerAdapter(this, R.layout.player_card, game.getPlayers());
        grid.setAdapter(myAdapter);
        registerForContextMenu(grid);
        day = false;

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
            dp = null;
            final Intent intent = new Intent();

            while(true){
                dp = mService.getPacket();

                if(dp != null) {
                    Log.i(this.getClass().getName(), "DataPacket received: " + dp.toString());
                }else{
                    Log.e(this.getClass().getName(), "DataPacket is null");
                    buildConfirmDialog(getResources().getString(R.string.general_connection_problem));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            builder.show();
                        }
                    });
                    return;
                }

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

                        //Play sound effect
                        intent.setClass(GameActivity.this, AudioBackground.class);
                        intent.putExtra("type", "FX");
                        intent.putExtra("sound", R.raw.msg);
                        startService(intent);
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

                        //Play sound effect
                        intent.setClass(GameActivity.this, AudioBackground.class);
                        intent.putExtra("type", "FX");
                        intent.putExtra("sound", R.raw.msg);
                        startService(intent);
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

                        //Play sound effect
                        intent.setClass(GameActivity.this, AudioBackground.class);
                        intent.putExtra("type", "FX");
                        intent.putExtra("sound", R.raw.msg);
                        startService(intent);
                        break;
                    case REFRESH_PLAYERS:

                        //refreshed players - who is alive and who's not
                        game.getPlayers().clear();
                        game.getPlayers().addAll(dp.getPlayers());
                        player = game.getPlayers().get(game.getPlayers().indexOf(player));
                        game.getPlayers().remove(player);

                        //Animation if player was murdered
                        if(dp.getPlayer() != null) {
                            msg = String.format(
                                    "<font color=\"#0066ff\">*" +
                                            getResources().getString(R.string.game_murdered) +
                                    "*</font><br/>", dp.getPlayer().getUsername());
                            anim.putExtra("animation", "file:///android_asset/murder.html");
                            announce.putExtra("msg", Html.fromHtml(msg).toString());
                            announce.removeExtra("icon");
                            startActivity(anim);
                            startActivity(announce);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myAdapter.notifyDataSetChanged();
                                    txtGameChat.append(Html.fromHtml(msg));
                                }
                            });

                            //Play sound effect
                            intent.setClass(GameActivity.this, AudioBackground.class);
                            intent.putExtra("type", "FX");
                            intent.putExtra("sound", R.raw.murder);
                            startService(intent);
                        }
                        break;
                    case DAY:
                        //reset target
                        GameActivity.this.target = null;

                        //day cycle - change GUI and chat
                        day = true;
                        msg = "<font color=\"#0066ff\">*"+ getResources().getString(R.string.game_day_phase) + "*</font><br/>";
                        game.getPlayers().clear();
                        game.getPlayers().addAll(dp.getPlayers());
                        player = game.getPlayers().get(game.getPlayers().indexOf(player));
                        game.getPlayers().remove(player);
                        if(GameActivity.this.player.isAlive()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtSendMessage.setEnabled(true);
                                }
                            });
                        }

                        anim.putExtra("animation","file:///android_asset/moon.html");
                        startActivity(anim);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyDataSetChanged();
                                imgvGamePhase.setImageResource(R.drawable.day);
                                txtGameChat.append(Html.fromHtml(msg));
                                txtSendMessage.setText("");

                                //reset button highlight
                                if(btn_target != null) {
                                    btn_target.setBackgroundResource(R.drawable.border_rectangle_inverted);
                                }
                                countDownTimer = new CountDownTimer(60000, 1000) {
                                    @Override
                                    public void onTick(long millisRemaining) {
                                        txtGameTimer.setText(String.valueOf(millisRemaining / 1000));
                                    }

                                    @Override
                                    public void onFinish() {
                                        if(GameActivity.this.player.isAlive()) {
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
                                        }
                                        txtGameTimer.setText("");
                                    }
                                }.start();
                            }
                        });

                        //Play sound effect
                        intent.setClass(GameActivity.this, AudioBackground.class);
                        intent.putExtra("type", "FX");
                        intent.putExtra("sound", R.raw.morning);
                        startService(intent);
                        break;
                    case NIGHT:
                        //reset target
                        GameActivity.this.target = null;

                        //night cycle - change GUI and chat
                        day = false;
                        msg = "<font color=\"#0066ff\">*" + getResources().getString(R.string.game_night_phase) + "*</font><br/>";
                        game.getPlayers().clear();
                        game.getPlayers().addAll(dp.getPlayers());
                        player = game.getPlayers().get(game.getPlayers().indexOf(player));
                        game.getPlayers().remove(player);
                        if(GameActivity.this.player.isAlive()) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    txtSendMessage.setEnabled(false);
                                }
                            });
                        }

                        anim.putExtra("animation","file:///android_asset/moon.html");
                        startActivity(anim);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyDataSetChanged();
                                txtGameChat.append(Html.fromHtml(msg));
                                imgvGamePhase.setImageResource(R.drawable.night);
                                txtSendMessage.setText(R.string.game_txtSendMessage_blocked);

                                //reset button highlight
                                if(btn_target != null) {
                                    btn_target.setBackgroundResource(R.drawable.border_rectangle_inverted);
                                }
                                countDownTimer = new CountDownTimer(30000, 1000) {
                                    @Override
                                    public void onTick(long millisRemaining) {
                                        txtGameTimer.setText(String.valueOf(millisRemaining / 1000));
                                    }

                                    @Override
                                    public void onFinish() {
                                        if (GameActivity.this.player.isAlive()) {
                                            new AsyncTask<Void, Void, Void>() {
                                                @Override
                                                protected Void doInBackground(Void... voids) {
                                                    DataPacket dp = GameActivity.this.player.getRole().action(new DataPacket());
                                                    dp.setPlayer(GameActivity.this.target);
                                                    mService.sendPacket(dp);
                                                    return null;
                                                }
                                            }.execute();
                                        }
                                        txtGameTimer.setText("");
                                    }
                                }.start();
                            }
                        });

                        //Play sound effect
                        intent.setClass(GameActivity.this, AudioBackground.class);
                        intent.putExtra("type", "FX");
                        intent.putExtra("sound", R.raw.night);
                        startService(intent);

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
                                getResources().getString(R.string.game_snitch_identity),dp.getPlayer().getUsername());
                        Roles role = dp.getPlayer().getRole();
                        if(role == Roles.KILLER){
                            msg = msg.concat(" " + getResources().getString(R.string.game_role_killer));
                        }else{
                            msg = msg.concat(" " + getResources().getString(R.string.game_role_notKiller));
                        }
                        msg = msg.concat("*</font><br/>");
                        announce.putExtra("msg", Html.fromHtml(msg).toString());
                        announce.removeExtra("icon");
                        startActivity(announce);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtGameChat.append(Html.fromHtml(msg));
                            }
                        });

                        //Play sound effect
                        intent.setClass(GameActivity.this, AudioBackground.class);
                        intent.putExtra("type", "FX");
                        intent.putExtra("sound", R.raw.action);
                        startService(intent);
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
                        anim.putExtra("animation","file:///android_asset/execute.html");
                        announce.putExtra("msg", Html.fromHtml(msg).toString());
                        announce.removeExtra("icon");
                        startActivity(anim);
                        startActivity(announce);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtGameChat.append(Html.fromHtml(msg));


                            }
                        });

                        intent.setClass(GameActivity.this, AudioBackground.class);
                        intent.putExtra("type", "FX");
                        intent.putExtra("sound", R.raw.action);
                        startService(intent);
                        break;
                    case HEAL:
                        msg = String.format(
                                "<font color=\"#0066ff\">*" +
                                        getResources().getString(R.string.game_attemptedMurder) +
                                "*</font><br/>");
                        announce.putExtra("msg", Html.fromHtml(msg).toString());
                        announce.removeExtra("icon");
                        startActivity(announce);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtGameChat.append(Html.fromHtml(msg));
                            }
                        });

                        intent.setClass(GameActivity.this, AudioBackground.class);
                        intent.putExtra("type", "FX");
                        intent.putExtra("sound", R.raw.action);
                        startService(intent);
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
                                txtGameChat.append(Html.fromHtml(msg));
                            }
                        });


                        intent.setClass(GameActivity.this, AudioBackground.class);
                        intent.putExtra("type", "FX");
                        intent.putExtra("sound", R.raw.action);
                        startService(intent);
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
                                txtGameChat.append(Html.fromHtml(msg));
                            }
                        });
                        break;
                    case READY:

                        //game begun (30 seconds wait and starting DAY phase) - get players
                        game.getPlayers().clear();
                        game.getPlayers().addAll(dp.getPlayers());
                        player = game.getPlayers().get(game.getPlayers().indexOf(player));
                        final String gameRole = String.format(
                                getResources().getString(R.string.game_player_role) , player.getRole().name());
                        game.getPlayers().remove(player);

                        int icon = -1;
                        switch (player.getRole()){
                            case KILLER:
                                icon = R.drawable.killer;
                                break;
                            case CITIZEN:
                                icon = R.drawable.citizen;
                                break;
                            case HEALER:
                                icon = R.drawable.doctor;
                                break;
                            case SNITCH:
                                icon = R.drawable.snitch;
                                break;
                        }
                        msg = String.format(getResources().getString(R.string.game_player_role), player.getRole().name());
                        announce.putExtra("msg", Html.fromHtml(msg).toString());
                        announce.putExtra("icon", icon);

                        msg =  "<font color=\"#0066ff\">*" +
                                getResources().getString(R.string.game_ready_phase) +
                                "*</font><br/>";

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

                                        //first night cycle is when the game truly starts
                                        gameStarted = true;
                                    }
                                }.start();
                                myAdapter.notifyDataSetChanged();
                                txtGameChat.append(Html.fromHtml(msg));
                                playerRole.setText(gameRole);
                                startActivity(announce);
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

                        //Update user's stats
                        GameActivity.this.player = dp.getPlayers().get(dp.getPlayers().indexOf(GameActivity.this.player));
                        ((GlobalResources)getApplication()).getPlayer().setStats(GameActivity.this.player.getStats());

                        announce.putExtra("msg", msg);
                        announce.removeExtra("icon");
                        startActivity(announce);

                        //Play sound effect
                        intent.setClass(GameActivity.this, AudioBackground.class);
                        intent.putExtra("type", "FX");
                        intent.putExtra("sound", R.raw.victory);
                        startService(intent);

                        intent.setClass(GameActivity.this, Lobby.class);
                        intent.removeExtra("type");
                        intent.removeExtra("sound");
                        startActivity(intent);
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

                        //Update user's stats
                        GameActivity.this.player = dp.getPlayers().get(dp.getPlayers().indexOf(GameActivity.this.player));
                        ((GlobalResources)getApplication()).getPlayer().setStats(GameActivity.this.player.getStats());

                        announce.putExtra("msg", msg);
                        announce.removeExtra("icon");
                        startActivity(announce);

                        intent.setClass(GameActivity.this, AudioBackground.class);
                        intent.putExtra("type", "FX");
                        intent.putExtra("sound", R.raw.victory);
                        startService(intent);

                        intent.setClass(GameActivity.this, Lobby.class);
                        intent.removeExtra("type");
                        intent.removeExtra("sound");
                        startActivity(intent);
                        finish();
                        return;
                    case GAME_DISBANDED:
                        buildConfirmDialog(getResources().getString(R.string.game_game_disbanded));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.show();
                            }
                        });

                        intent.setClass(GameActivity.this, Lobby.class);
                        intent.removeExtra("type");
                        intent.removeExtra("sound");
                        startActivity(intent);
                        return;
                    case DISCONNECT:

                        //In-case the user disconnected mid-game, update stats (negative rating)
                        if(dp.getPlayer() != null) {
                            ((GlobalResources) getApplication()).getPlayer().setStats(dp.getPlayer().getStats());
                        }
                        return;
                    case SERVER_SHUTDOWN:
                        buildConfirmDialog(getResources().getString(R.string.general_server_shutdown));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.show();
                            }
                        });
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
            final Button btnPlayerAction = (Button) convertView.findViewById(R.id.playerCard_btn_action);
            final Roles role = GameActivity.this.player.getRole();

            txtPlayerName.setText(user.getUsername());
            txtPlayerStatus.setText("");

            if(gameStarted) {
                if (user.isAlive()) {
                    txtPlayerStatus.setText(getResources().getText(R.string.game_player_alive));
                    if(GameActivity.this.player.isAlive()){
                        //Day phase is only for voting, night phase is for actions
                        if (role != null) {
                            if (!day) {
                                btnPlayerAction.setVisibility(View.VISIBLE);
                                if (role == Roles.KILLER) {
                                    btnPlayerAction.setText(getResources().getText(R.string.game_kill));
                                } else if (role == Roles.HEALER) {
                                    btnPlayerAction.setText(getResources().getText(R.string.game_heal));
                                } else if (role == Roles.SNITCH) {
                                    btnPlayerAction.setText(getResources().getText(R.string.game_snitch));
                                } else {
                                    btnPlayerAction.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                btnPlayerAction.setText(getResources().getText(R.string.game_vote));
                                btnPlayerAction.setVisibility(View.VISIBLE);
                            }
                        }
                    }else{
                        btnPlayerAction.setVisibility(View.INVISIBLE);
                    }
                } else {
                    txtPlayerStatus.setText(getResources().getText(R.string.game_player_dead));
                    btnPlayerAction.setVisibility(View.INVISIBLE);
                }
                txtPlayerStatus.setVisibility(View.VISIBLE);
            }else{
                txtPlayerStatus.setVisibility(View.INVISIBLE);
                btnPlayerAction.setVisibility(View.INVISIBLE);
            }

            btnPlayerAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                // Check if the user is alive or not as well as the user the
                //action is set upon
                if(GameActivity.this.player.isAlive()) {
                    if (user.isAlive()) {

                        //If target is the same, set to null and affect GUI
                        if(GameActivity.this.target != null && GameActivity.this.target.getUsername().equals(user.getUsername())){
                            GameActivity.this.target = null;
                            btnPlayerAction.setBackgroundResource(R.drawable.border_rectangle_inverted);
                        }else {

                            //if target is not the same, affect GUI of previous target if there was any
                            if(GameActivity.this.target != null){

                                //change GUI from previous target
                                GameActivity.this.btn_target.setBackgroundResource(R.drawable.border_rectangle_inverted);
                            }
                            GameActivity.this.target = user;
                            GameActivity.this.btn_target = btnPlayerAction;
                            btnPlayerAction.setBackgroundResource(R.drawable.border_rectangle_red);
                            Log.i(GameActivity.this.getClass().getName(), "Target: " + GameActivity.this.target.toString());
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
                    if(countDownTimer != null) {
                        countDownTimer.cancel();
                    }
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

    public void buildConfirmDialog(final String msg) {
        builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.general_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                GameActivity.this.finish();
                if(!msg.equals(R.string.game_game_disbanded)) {
                    ClientConnection.getConnection().closeSocket();
                }
            }
        });
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        buildExitDialog();
        builder.show();
    }
}

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
import android.text.Spanned;
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

import java.util.ArrayList;
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
    private GameService mService;
    private boolean mBound = false;
    private MyPlayerAdapter myAdapter;
    private CountDownTimer countDownTimer;
    private boolean day;
    private boolean gameStarted;
    private boolean nobodyMurdered;
    private String msg;
    private Intent anim;
    private Intent announce;
    private TextView playerRole;
    private GameLogic gameLogic;
    private static final int ANIMATION_AND_ANNOUNCE = 1;

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
        player.setAlive(false);
        gameLogic = new GameLogic();
        myAdapter = new MyPlayerAdapter(this, R.layout.player_card, game.getPlayers());
        grid.setAdapter(myAdapter);
        registerForContextMenu(grid);
        day = false;
        nobodyMurdered = true;
    }

    class GameLogic extends Thread{

        @Override
        public void run() {

            //start game service
            DataPacket dp = null;
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
                    this.interrupt();
                    return;
                }

                switch(dp.getCommand()){
                    case SEND_MESSAGE:
                        msg = "<font color=\"#009933\">" +
                                dp.getPlayer().getUsername() +
                                ": </font><font color=\"#ffffff\">" +
                                dp.getMessage() +
                                "</font><br/>";
                        addToChat(Html.fromHtml(msg));
                        playSoundEffect(R.raw.msg);
                        break;
                    case SEND_MESSAGE_DEAD:
                        msg = "<font color=\"#cc0000\">*DEAD*</font><font color=\"#009933\">" +
                                dp.getPlayer().getUsername() +
                                ": </font><font color=\"#ffffff\">"
                                + dp.getMessage() +
                                "</font><br/>";
                        addToChat(Html.fromHtml(msg));
                        playSoundEffect(R.raw.msg);
                        break;
                    case SEND_MESSAGE_KILLER:
                        msg = "<font color=\"#ff3300\">" +
                                dp.getPlayer().getUsername() +
                                ": </font><font color=\"#ffffff\">" +
                                dp.getMessage() +
                                "</font><br/>";
                        addToChat(Html.fromHtml(msg));
                        playSoundEffect(R.raw.msg);
                        break;
                    case REFRESH_PLAYERS:
                        Spanned message = null;
                        if(dp.getPlayer() != null) {

                            //flag that someone was murdered
                            nobodyMurdered = false;

                            //Animation if player was murdered
                            refreshPlayers(dp.getPlayers());

                            //If this player is dead, show appropriate message
                            if (!GameActivity.this.player.getUsername().equals(dp.getPlayer().getUsername())) {
                                message = Html.fromHtml(String.format(
                                        "<font color=\"#0066ff\">*" +
                                                getResources().getString(R.string.game_murdered) +
                                                "*</font><br/>", dp.getPlayer().getUsername()));
                            } else {
                                message = Html.fromHtml(String.format(
                                        "<font color=\"#0066ff\">*" +
                                                getResources().getString(R.string.game_you_murdered) +
                                                "*</font><br/>", dp.getPlayer().getUsername()));

                                //Indicate that the player is dead
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playerRole.append(" (" + getString(R.string.game_player_dead) + ")");
                                    }
                                });
                            }


                            addToChat(message);
                            playAnimation("file:///android_asset/murder.html", message.toString(), -1, R.raw.murder);

                            try {
                                Thread.sleep(8500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case DAY:

                        //day cycle - change GUI and chat
                        day = true;

                        //if someone was murdered
                        if(nobodyMurdered){
                            msg = "<font color=\"#0066ff\">*" +
                                        getResources().getString(R.string.game_night_no_murder) +
                                    "*</font><br/>";
                            addToChat(Html.fromHtml(msg));
                            showAnnouncement(Html.fromHtml(msg).toString(), -1, R.raw.action);

                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else{

                            //reset for the next time
                            nobodyMurdered = true;
                        }

                        msg = "<font color=\"#0066ff\">*"+ getResources().getString(R.string.game_day_phase) + "*</font><br/>";
                        refreshPlayers(dp.getPlayers());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtSendMessage.setEnabled(true);
                            }
                        });

                        addToChat(Html.fromHtml(msg));
                        playAnimation("file:///android_asset/sun.html", null , -1, R.raw.morning);
                        phaseChange(R.drawable.day, "", 60000, Commands.VOTE);
                        break;
                    case NIGHT:

                        //night cycle - change GUI and chat
                        day = false;

                        //fixes problems with player buttons appearing / disappearing in the wrong phases
                        gameStarted = true;

                        msg = "<font color=\"#0066ff\">*" + getResources().getString(R.string.game_night_phase) + "*</font><br/>";
                        refreshPlayers(dp.getPlayers());
                        if(GameActivity.this.player.isAlive()) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    txtSendMessage.setEnabled(false);
                                }
                            });
                        }

                        addToChat(Html.fromHtml(msg));
                        playAnimation("file:///android_asset/moon.html", null, -1, R.raw.night);
                        final Commands command = GameActivity.this.player.getRole().action(new DataPacket()).getCommand();
                        phaseChange(R.drawable.night, getString(R.string.game_txtSendMessage_blocked),
                                    30000, command);

                        //This code will be relevant when there are more than 5 players
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
                                getResources().getString(R.string.game_snitch_identity), dp.getPlayer().getUsername());
                        Roles role = dp.getPlayer().getRole();
                        if(role == Roles.KILLER){
                            msg = msg.concat(" " + getResources().getString(R.string.game_role_killer));
                        }else{
                            msg = msg.concat(" " + getResources().getString(R.string.game_role_notKiller));
                        }
                        msg = msg.concat("*</font><br/>");
                        addToChat(Html.fromHtml(msg));
                        showAnnouncement(Html.fromHtml(msg).toString(), -1, R.raw.action);

                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case EXECUTE:

                        //player was executed - get datapacket with player who was killed
                        msg = "<font color=\"#0066ff\">*";
                        if (player.equals(dp.getPlayer())) {
                           msg = msg.concat(getResources().getString(R.string.game_executed_you));

                            //If the player died, indicate it
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    playerRole.append(" (" + getString(R.string.game_player_dead) + ")");
                                }
                            });
                        } else {
                            msg = msg.concat(String.format(
                                    getResources().getString(R.string.game_executed_other),
                                    dp.getPlayer().getUsername()));
                        }
                        msg = msg.concat("*</font><br/>");
                        addToChat(Html.fromHtml(msg));
                        playAnimation("file:///android_asset/execute.html" ,Html.fromHtml(msg).toString(), -1, R.raw.action);

                        try {
                            Thread.sleep(8000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case HEAL:
                        msg = "<font color=\"#0066ff\">*" +
                                        getResources().getString(R.string.game_attemptedMurder) +
                               "*</font><br/>";
                        addToChat(Html.fromHtml(msg));
                        showAnnouncement(Html.fromHtml(msg).toString(), -1, R.raw.action);

                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case PLAYER_JOINED:

                        //get a player and players (alert about the player and set the players)
                        msg = String.format(
                                "<font color=\"#0066ff\">*" +
                                getResources().getString(R.string.game_player_joined) +
                                "*</font><br/>", dp.getPlayer().getUsername());
                        game.getPlayers().add(dp.getPlayer());
                        game.getPlayers().remove(GameActivity.this.player);
                        addToChat(Html.fromHtml(msg));
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
                        addToChat(Html.fromHtml(msg));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtGameTimer.setText((game.getPlayers().size() + 1) + " / " + game.getMaxPlayers());
                                myAdapter.notifyDataSetChanged();
                            }
                        });
                        break;
                    case READY:

                        refreshPlayers(dp.getPlayers());

                        int icon = -1;
                        String roleTxt = "";
                        switch (player.getRole()){
                            case KILLER:
                                icon = R.drawable.killer;
                                roleTxt = getResources().getString(R.string.game_role_killer);
                                break;
                            case CITIZEN:
                                icon = R.drawable.citizen;
                                roleTxt = getResources().getString(R.string.game_role_citizen);
                                break;
                            case HEALER:
                                icon = R.drawable.doctor;
                                roleTxt = getResources().getString(R.string.game_role_healer);
                                break;
                            case SNITCH:
                                icon = R.drawable.snitch;
                                roleTxt = getResources().getString(R.string.game_role_snitch);
                                break;
                        }
                        final String gameRole = String.format(
                                getResources().getString(R.string.game_player_role) , roleTxt);
                        showAnnouncement(gameRole, icon, R.raw.action);

                        final String readyMsg =  "<font color=\"#0066ff\">*" +
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
                                    }
                                }.start();

                                //update the player's role
                                playerRole.setText(gameRole);
                            }
                        });
                        roleTxt = null;
                        break;
                    case VOTE_DRAW:
                        msg = "<font color=\"#0066ff\">*" +
                                getResources().getString(R.string.game_vote_tie) +
                                "*</font><br/>";
                        addToChat(Html.fromHtml(msg));
                        showAnnouncement(Html.fromHtml(msg).toString(), -1, R.raw.action);

                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case WIN_CITIZENS:

                        //get refreshed players with stats and game history - check each one for this player
                        msg = getResources().getString(R.string.game_citizens_win);
                        endGame(msg, dp.getPlayers());
                        this.interrupt();
                        return;
                    case WIN_KILLERS:

                        //get refreshed players with stats and game history - check each one for this player
                        msg = getResources().getString(R.string.game_killers_win);
                        endGame(msg, dp.getPlayers());
                        this.interrupt();
                        return;
                    case GAME_DISBANDED:
                        buildConfirmDialog(getResources().getString(R.string.game_game_disbanded));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.show();
                            }
                        });
                        this.interrupt();
                        return;
                    case DISCONNECT:

                        //In-case the user disconnected mid-game, update stats (negative rating)
                        if(dp.getPlayer() != null) {
                            ((GlobalResources) getApplication()).getPlayer().setStats(dp.getPlayer().getStats());
                        }
                        intent.setClass(GameActivity.this, Lobby.class);
                        startActivity(intent);
                        GameActivity.this.finish();
                        this.interrupt();
                        return;
                    case SERVER_SHUTDOWN:
                        buildConfirmDialog(getResources().getString(R.string.general_server_shutdown));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.show();
                            }
                        });
                        this.interrupt();
                        return;
                }
            }
        }
    }

    /**
     * Add messages to the game chat
     * @param message The message to append
     */
    private void addToChat(final Spanned message){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    txtGameChat.append(message);
            }
        });
    }

    /**
     * Play sound effect
     * @param soundEffectResource Sound effect resource to play, e.g. R.raw.sound
     */
    private void playSoundEffect(final int soundEffectResource){
        final Intent intent = new Intent(GameActivity.this, AudioBackground.class);
        intent.putExtra("type", "FX");
        intent.putExtra("sound", soundEffectResource);
        startService(intent);
    }

    /**
     * Play an animation with or without an announcement and/or an icon
     * @param animationResource Animation to play
     * @param message Message to announce (can be null)
     * @param iconResource Icon to show with announcement (-1 to cancel)
     * @param soundEffect Sound effect to play with the animation
     */
    private void playAnimation(final String animationResource, final String message, final int iconResource, final int soundEffect){
        anim.putExtra("animation", animationResource);
        anim.putExtra("sfx", soundEffect);
        if(message != null){
            announce.putExtra("msg", message);

            //if there's an icon, add it
            if(iconResource != -1) {
                announce.putExtra("icon", iconResource);
            }else{
                announce.removeExtra("icon");
            }
            startActivityForResult(announce, ANIMATION_AND_ANNOUNCE);
        }else{

            //In case there's also an announcement, synchronize animation and announcement
            announce.removeExtra("msg");
            announce.removeExtra("icon");
            startActivity(anim);
        }
    }

    /**
     * Shows a game announcement with a message, an icon and a sound effect
     * @param message The message to announce
     * @param iconResource The icon to be shown with the message (-1 to cancel)
     * @param soundEffect The sound effect resource to play (-1 to cancel)
     */
    private void showAnnouncement(final String message, final int iconResource, final int soundEffect){
        announce.putExtra("msg", message);
        if(iconResource != -1) {
            announce.putExtra("icon", iconResource);
        }else{
            announce.removeExtra("icon");
        }
        if(soundEffect != -1){
            announce.putExtra("sfx", soundEffect);
        }else{
            announce.removeExtra("sfx");
        }
        startActivity(announce);
    }

    /**
     * Update the adapter with the new changes to the players list
     * @param playersList The new list of players
     */
    private void refreshPlayers(final List<Player> playersList){
        game.getPlayers().clear();
        game.getPlayers().addAll(playersList);
        player = game.getPlayers().get(game.getPlayers().indexOf(player));
        game.getPlayers().remove(player);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Change the GUI and update the game window based on the phase
     * @param phaseImageResource Phase's image resource
     * @param phaseString Phase's string resource for txtSendMessage
     * @param delay Phase's timer delay in miliseconds( 1 minute = 60000 )
     * @param command Phase's command to be sent (Day = VOTE, Night = Player's role action)
     */
    private void phaseChange(final int phaseImageResource, final String phaseString, final int delay, final Commands command){

        //reset target
        GameActivity.this.target = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgvGamePhase.setImageResource(phaseImageResource);

                //Players who are alive should be aware that at night the chat is blocked.
                //On the other hand, dead players should be able to chat freely
                if(player.isAlive()) {
                    txtSendMessage.setHint(phaseString);
                }else{
                    txtSendMessage.setHint("");
                }

                //reset button highlight
                if (btn_target != null) {
                    btn_target.setBackgroundResource(R.drawable.border_rectangle_inverted);
                }

                countDownTimer = new CountDownTimer(delay, 1000) {
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
                                    DataPacket dp = new DataPacket();
                                    dp.setCommand(command);
                                    dp.setPlayer(GameActivity.this.target);
                                    mService.sendPacket(dp);
                                    return null;
                                }
                            }.execute();
                        }
                        txtGameTimer.setText(R.string.game_wait);
                    }
                }.start();
            }
        });
    }

    /**
     * End the game, display the message based on the result and update the application's player data
     * and finish the activity
     * @param message Message to display
     * @param players Updated player's list
     */
    private void endGame(final String message, final List<Player> players){

        //Update user's stats
        GameActivity.this.player = players.get(players.indexOf(GameActivity.this.player));
        ((GlobalResources)getApplication()).getPlayer().setStats(GameActivity.this.player.getStats());
        //showAnnouncement(message, -1, R.raw.victory);

        //show who was the killer
        String killer = "";

        if(GameActivity.this.player.getRole() != Roles.KILLER) {
            for (Player p : GameActivity.this.game.getPlayers()){
                if (p.getRole() == Roles.KILLER) {
                    killer = p.getUsername();
                    break;
                }
            }
        }else{
            killer = GameActivity.this.player.getUsername();
        }
        playSoundEffect(R.raw.victory);
        buildConfirmDialog(message + "\n" + getString(R.string.game_end_show_killer) + " " + killer);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
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

                            if (player.isAlive()) {
                                if (day || (!(gameStarted))) {
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

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            txtSendMessage.setText("");
                        }
                    }.execute();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ANIMATION_AND_ANNOUNCE){
            if(resultCode == RESULT_OK){
                startActivity(anim);
            }
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

    @Override
    protected void onResume() {
        super.onResume();
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
                if(gameLogic.getState() == Thread.State.NEW) {
                    gameLogic.start();
                }
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
            if(player.isAlive()) {
                builder.setMessage(getResources().getText(R.string.game_exitDialog));
            }else{
                builder.setMessage(getResources().getText(R.string.general_exitDialog));
            }
            builder.setCancelable(false);

            builder.setPositiveButton(getResources().getString(R.string.general_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    DataPacket dp = new DataPacket();
                    dp.setCommand(Commands.DISCONNECT);
                    mService.sendPacket(dp);
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
                if(msg.equals(getResources().getString(R.string.general_connection_problem)) ||
                        msg.equals(getResources().getString(R.string.general_server_shutdown)))  {
                    ClientConnection.getConnection().closeSocket();
                    finish();
                }else{
                    final Intent intent = new Intent(GameActivity.this, Lobby.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        buildExitDialog();
        builder.show();
    }

    @Override
    protected void onDestroy() {
        if(countDownTimer != null){
            countDownTimer.cancel();
        }
        if(gameLogic.isAlive()){
            gameLogic.interrupt();
        }
        super.onDestroy();
    }
}

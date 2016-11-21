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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;
import ben_and_asaf_ttp.thetownproject.shared_resources.PlayerStatus;
import ben_and_asaf_ttp.thetownproject.shared_resources.Roles;


public class Lobby extends AppCompatActivity implements View.OnClickListener{

    private Player player;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private TextView txtPlayer;
    private GlobalResources globalResources;
    //private AlertDialog dialogHowManyPlayers;
    private ProgressDialog dialogProgress;
    //private int numPlayers = -1;
    private GameService mService;
    private boolean mBound = false;
    private MyPlayerAdapter myAdapter;
    private AlertDialog.Builder builder;
    private Timer timer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        globalResources = (GlobalResources)getApplication();
        player = globalResources.getPlayer();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        //Toolbar toolbar = (Toolbar)findViewById(R.id.lobby_toolbar);
        //setSupportActionBar(toolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.accessibility_open_drawer, R.string.accessibility_close_drawer){

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                //When the drawer closes, it cancels the timer
                timer = new Timer();
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {

                        //run refresh friends every 8 seconds (delayed start by 500 ms)
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                DataPacket dp = new DataPacket();
                                dp.setCommand(Commands.REFRESH_FRIENDS);
                                mService.sendPacket(dp);
                            }
                        }, 500, 8000);
                        return null;
                    }
                }.execute();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                //cancels the timer and stops refreshing friend list
                timer.cancel();
            }
        };

        // Add the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // Set the adapter for the list view
        myAdapter = new MyPlayerAdapter(this, R.layout.friend_card, player.getFriends());
        mDrawerList.setAdapter(myAdapter);
        mDrawerLayout.setOnClickListener(this);

        Button btnJoinGame = (Button)findViewById(R.id.lobby_btn_joinGame);
        Button btnOptions = (Button)findViewById(R.id.lobby_btn_options);
        Button btnStats = (Button)findViewById(R.id.lobby_btn_stats);

        btnJoinGame.setOnClickListener(this);
        btnOptions.setOnClickListener(this);
        btnStats.setOnClickListener(this);

        //set the player's name as a greeting
        txtPlayer = (TextView) findViewById(R.id.lobby_txt_player);
        txtPlayer.setText(player.getUsername());

        //How many players dialog that handles the player's request
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(getResources().getText(R.string.lobby_matchMaking_howManyPlayers_title));
//        builder.setSingleChoiceItems(R.array.lobby_array_how_many_players, -1, new DialogInterface.OnClickListener(){
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                //set value according to the array (QuickJoin[0], 5, 8 or 10)
//                if(which == 0){
//
//                    //if it's quick-join, set default of 0 (which is quick-join in the server)
//                    Lobby.this.numPlayers = 0;
//                }else {
//                    Lobby.this.numPlayers = Integer.decode(getResources().getStringArray(R.array.lobby_array_how_many_players)[which]);
//                }
//                Lobby.this.dialogHowManyPlayers.dismiss();
//                Lobby.this.dialogProgress.show();
//                new AsyncTask<Void, Void, DataPacket>(){
//
//                    @Override
//                    protected DataPacket doInBackground(Void... params) {
//                        DataPacket dp = new DataPacket();
//                        dp.setCommand(Commands.READY);
//                        dp.setNumber(Lobby.this.numPlayers);
//                        ClientConnection.getConnection().sendDataPacket(dp);
//                        dp = ClientConnection.getConnection().receiveDataPacket();
//                        return dp;
//                    }
//
//                    // this runs on UI thread
//                    @Override
//                    protected void onPostExecute(DataPacket dp) {
//                        if(dp.getCommand() == Commands.OK) {
//                            ((GlobalResources) getApplication()).setGame(dp.getGame());
//                            if (((GlobalResources) getApplication()).getGame() != null) {
//                                dialogProgress.dismiss();
//                            }
//                        }
//                        Intent myIntent = new Intent(Lobby.this, GameActivity.class);
//                        startActivity(myIntent);
//                        Lobby.this.finish();
//                    }
//                }.execute();
//            }
//        });
//
//        dialogHowManyPlayers = builder.create();

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

    class LobbyLogic implements Runnable{

        @Override
        public void run() {

            //start game service
            DataPacket dp = null;
            String msg;

            while(true){
                dp = mService.getPacket();
                Intent myIntent;

                switch(dp.getCommand()){
                    case OK:
                        ((GlobalResources) getApplication()).setGame(dp.getGame());
                        ((GlobalResources) getApplication()).setPlayer(Lobby.this.player);
                        if (((GlobalResources) getApplication()).getGame() != null) {
                            dialogProgress.dismiss();
                        }
                        myIntent = new Intent(Lobby.this, GameActivity.class);
                        startActivity(myIntent);
                        Lobby.this.finish();
                        break;
                    case REFRESH_FRIENDS:
                        Lobby.this.player.getFriends().clear();
                        Lobby.this.player.getFriends().addAll(dp.getPlayers());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyDataSetChanged();
                            }
                        });
                        break;
                    case FRIEND_REQUEST:
                        //TODO: Snackbar with forward to activity that handles friend requests
                        break;
                    case SERVER_SHUTDOWN:
                        buildConfirmDialog(getResources().getString(R.string.general_server_shutdown));
                        builder.show();
                        break;
                    default:
                        if(dp != null) {
                            Log.i(this.getClass().getName(), "DataPacket received: " + dp.toString());
                        }else{
                            Log.e(this.getClass().getName(), "DataPacket is null");
                        }
                        return;
                }
                Log.i(this.getClass().getName(), "DataPacket received: " + dp.toString());
            }
        }
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
        buildExitDialog();
        builder.show();
    }

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

            // Lookup view for data population
            final TextView txtPlayerName = (TextView) convertView.findViewById(R.id.friendcard_txt_username);
            final TextView txtPlayerStatus = (TextView) convertView.findViewById(R.id.friendcard_txt_status);

            txtPlayerName.setText(user.getUsername());
            txtPlayerStatus.setText(user.getStatus().name());

            txtPlayerName.setOnCreateContextMenuListener(this);
            return convertView;
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {}
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friendcard_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Player p = Lobby.this.player.getFriends().get(info.position);
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_friendcard_joingame:
                if(p.getStatus() == PlayerStatus.INQUEUE) {
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... params) {
                            DataPacket dp = new DataPacket();
                            dp.setCommand(Commands.JOIN_FRIEND);
                            dp.setPlayer(p);
                            mService.sendPacket(dp);
                            return null;
                        }
                    }.execute();
                }else{
                    Toast.makeText(this, "Friend is not playing",Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_friendcard_remove:
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        DataPacket dp = new DataPacket();
                        dp.setCommand(Commands.REMOVE_FRIEND);
                        dp.setPlayer(p);
                        mService.sendPacket(dp);
                        return null;
                    }
                }.execute();
                return true;
            case R.id.menu_friendcard_showstats:
                intent = new Intent(this, StatsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void buildConfirmDialog(String msg) {
        if(builder == null) {
            builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setPositiveButton(getResources().getString(R.string.general_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Lobby.this.finish();
                    ClientConnection.getConnection().closeSocket();
                }
            });
        }
        builder.setMessage(msg);
    }

    public void buildExitDialog() {
        if(builder == null) {
            builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.lobby_disconnect));
            builder.setCancelable(false);

            builder.setPositiveButton(getResources().getString(R.string.general_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
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
                    Intent intent = new Intent(Lobby.this, MainActivity.class);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.general_no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
           case R.id.lobby_btn_joinGame:

               //Use this in-case multi-choice of players is needed, for now only 5
               //this.dialogHowManyPlayers.show();
               Lobby.this.dialogProgress.show();
               new AsyncTask<Void, Void, Void>(){

                   @Override
                   protected Void doInBackground(Void... params) {
                       DataPacket dp = new DataPacket();
                       dp.setCommand(Commands.READY);
                       dp.setNumber(5);
                       mService.sendPacket(dp);
                       return null;
                   }
               }.execute();
               break;
            case R.id.lobby_btn_options:
                Intent btnOptions = new Intent(Lobby.this,SettingsActivity.class);
                Lobby.this.startActivity(btnOptions);
                break;
            case R.id.lobby_btn_stats:
                Intent btnStats = new Intent(Lobby.this, StatsActivity.class);
                Lobby.this.startActivity(btnStats);
                break;
            //case R.id.friendlist_add_friend:
                //TODO: add friend activity
//                Intent friendlist_addFriend = new Intent(Lobby.this, AddFriend.class);
               // break;
            default:
                break;
        }
    }
}

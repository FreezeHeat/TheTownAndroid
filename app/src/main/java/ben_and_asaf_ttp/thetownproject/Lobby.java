package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;
import ben_and_asaf_ttp.thetownproject.shared_resources.PlayerStatus;


public class Lobby extends AppCompatActivity implements View.OnClickListener {
    private Player player;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private AlertDialog dialogJoinMethod;
    private ProgressDialog dialogProgress;
    private int numPlayers = -1;
    private GameService mService;
    private boolean mBound = false;
    private MyPlayerAdapter myAdapter;
    private AlertDialog.Builder builder;
    private AlertDialog.Builder builderFriends;
    private AlertDialog dialog;
    private ActionBarDrawerToggle mDrawerToggle;
    private Timer timer;
    private EditText username;
    private Button btnJoinGame;
    private Button btnGameGuide;
    private Executor executor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        player = ((GlobalResources)getApplication()).getPlayer();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        //set action bar
        setSupportActionBar(myToolbar);

        // Set the adapter for the list view
        myAdapter = new MyPlayerAdapter(this, R.layout.friend_card, player.getFriends());
        mDrawerList.setAdapter(myAdapter);
        registerForContextMenu(mDrawerList);
        mDrawerLayout.setOnClickListener(this);

        btnGameGuide = (Button)findViewById(R.id.lobby_btn_gameguide);
        btnJoinGame = (Button) findViewById(R.id.lobby_btn_joinGame);
        Button btnOptions = (Button) findViewById(R.id.lobby_btn_options);
        Button btnStats = (Button) findViewById(R.id.lobby_btn_stats);
        btnJoinGame.setOnClickListener(this);
        btnOptions.setOnClickListener(this);
        btnStats.setOnClickListener(this);
        btnGameGuide.setOnClickListener(this);

        //set the player's name as a greeting
        TextView txtPlayer = (TextView) findViewById(R.id.lobby_txt_player);
        txtPlayer.setText(player.getUsername());

        //How many players dialog that handles the player's request
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getText(R.string.lobby_matchMaking_joinmethod_title));
        builder.setSingleChoiceItems(R.array.lobby_matchmaking_joinmethod, 0, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which){
                    case 0:
                        //Join by rank
                        Lobby.this.numPlayers = 5;
                        break;

                    //Join any game
                    case 1:
                        Lobby.this.numPlayers = 0;
                        break;
                }
                Lobby.this.dialogJoinMethod.dismiss();
                Lobby.this.dialogProgress.show();
                new AsyncTask<Void, Void, Void>(){

                    @Override
                    protected Void doInBackground(Void... params) {
                        Lobby.this.dialogProgress.show();
                        DataPacket dp = new DataPacket();
                        dp.setCommand(Commands.READY);
                        dp.setNumber(Lobby.this.numPlayers);
                        ClientConnection.getConnection().sendDataPacket(dp);
                        executor.execute(new LobbyLogic());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Lobby.this.btnJoinGame.setEnabled(true);
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            }
        });

        dialogJoinMethod = builder.create();
        dialogJoinMethod.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                btnJoinGame.setEnabled(true);
            }
        });

        //EditText view for the "Add friend" menu
        username = new EditText(this);
        username.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        username.setHint(getResources().getText(R.string.lobby_insertUsername));

        //Only letters, numbers and underscores (REGEX)
        username.setFilters(new InputFilter[] {
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence cs, int start,
                                               int end, Spanned spanned, int dStart, int dEnd) {
                        // TODO Auto-generated method stub
                        if(cs.equals("")){ // for backspace
                            return cs;
                        }
                        if(cs.toString().matches("\\w+")){
                            return cs;
                        }
                        return "";
                    }
                }
        });
        username.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        //Add progress dialog when a player waits for a game to be found, he can also cancel this
        dialogProgress = new ProgressDialog(this);
        dialogProgress.setMessage(getResources().getString(R.string.lobby_matchMaking_progress_message));
        dialogProgress.setTitle(getResources().getString(R.string.lobby_matchMaking_progress_title));
        dialogProgress.setButton(DialogInterface.BUTTON_NEGATIVE,
                getResources().getText(R.string.lobby_matchMaking_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((GlobalResources) getApplication()).setGame(null);
                        dialogProgress.dismiss();
                        btnJoinGame.setEnabled(true);
                    }
                });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.accessibility_open_drawer,  /* "open drawer" description */
                R.string.accessibility_close_drawer  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if(timer != null) {
                    timer.cancel();
                }
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                //When the drawer closes, it cancels the timer
                timer = new Timer();

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {

                        //run refresh friends every X seconds (delayed start by 500 ms)
                        // X = 8 if has friends, if not X = 20
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                DataPacket dp = new DataPacket();
                                dp.setCommand(Commands.REFRESH_FRIENDS);
                                mService.sendPacket(dp);
                                executor.execute(new LobbyLogic());
                            }
                        }, 500, ((Lobby.this.player.getFriends().size() == 0) ? 20000 : 8000));
                        return null;
                    }
                }.execute();
                invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        //action bar configurations
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //execute lobby logic
        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        };

        //refresh friends upon start
        if(GameService.isRunning && mBound) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    DataPacket dp = new DataPacket();
                    dp.setCommand(Commands.REFRESH_FRIENDS);
                    mService.sendPacket(dp);
                    executor.execute(new LobbyLogic());
                    return null;
                }
            }.execute();
        }

        //media player
        final Intent myIntent = new Intent(Lobby.this, AudioBackground.class);
        myIntent.setClass(Lobby.this, AudioBackground.class);
        myIntent.putExtra("type", "BG");
        myIntent.putExtra("sound", R.raw.bg);
        startService(myIntent);
    }

    class LobbyLogic implements Runnable{

        @Override
        public void run() {

            //start game service
            DataPacket dp = mService.getPacket();
            Intent myIntent;

            switch(dp.getCommand()){
                case OK:
                    ((GlobalResources) getApplication()).setGame(dp.getGame());
                    ((GlobalResources) getApplication()).setPlayer(Lobby.this.player);
                    if (((GlobalResources) getApplication()).getGame() != null) {
                        dialogProgress.dismiss();
                    }
                    if(timer != null) {
                        timer.cancel();
                        timer.purge();
                    }
                    myIntent = new Intent(Lobby.this, GameActivity.class);
                    startActivity(myIntent);
                    Lobby.this.finish();
                    break;
                case REFRESH_FRIENDS:
                    Lobby.this.player.getFriends().clear();
                    if(dp.getPlayers() != null) {
                        Lobby.this.player.getFriends().addAll(dp.getPlayers());
                    }
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
                    dialog.show();
                    break;
                case WRONG_DETAILS:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Lobby.this, R.string.lobby_dialog_wrong_details, Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
            }
            Log.i(this.getClass().getName(), "DataPacket received: " + dp.toString());
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()){
            case R.id.lobby_add_friend:
                buildAddFriendDialog();
                dialog.show();
                break;
            case R.id.lobby_menu_website:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString("serverWs", "127.0.0.1")));
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        menu.findItem(R.id.lobby_add_friend).setVisible(false);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.lobby_add_friend).setVisible(drawerOpen);
        menu.findItem(R.id.lobby_menu_website).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        final Intent intent = new Intent(Lobby.this, AudioBackground.class);
        stopService(intent);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        if(AudioBackground.isPlaying()) {
            AudioBackground.getBg().pause();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        if(AudioBackground.isPlaying()) {
            AudioBackground.getBg().start();
        }
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
        dialog.show();
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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_card, parent, false);
            }

            // Lookup view for data population
            final TextView txtPlayerName = (TextView) convertView.findViewById(R.id.friendcard_txt_username);
            final TextView txtPlayerStatus = (TextView) convertView.findViewById(R.id.friendcard_txt_status);

            txtPlayerName.setText(user.getUsername());
            if(user.getStatus() == null){
                user.setStatus(PlayerStatus.OFFLINE);
            }

            switch (user.getStatus()){
                case OFFLINE:
                    txtPlayerStatus.setText(getString(R.string.lobby_friendstatus_offline));
                    break;
                case ONLINE:
                    txtPlayerStatus.setText(getString(R.string.lobby_friendstatus_online));
                    break;
                case INGAME:
                    txtPlayerStatus.setText(getString(R.string.lobby_friendstatus_ingame));
                    break;
                case INQUEUE:
                    txtPlayerStatus.setText(getString(R.string.lobby_friendstatus_inqueue));
                    break;
            }

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
                            executor.execute(new LobbyLogic());
                            return null;
                        }
                    }.execute();
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    Lobby.this.dialogProgress.show();
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
                ((GlobalResources)getApplication()).setStatsPlayer(p);
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void buildConfirmDialog(String msg) {
        builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.general_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Lobby.this.finish();
                ClientConnection.getConnection().closeSocket();
            }
        });
        dialog = builder.create();
    }

    public void buildAddFriendDialog() {
        if(builderFriends == null) {
            builderFriends = new AlertDialog.Builder(this);
            builderFriends.setView(username);
            builderFriends.setMessage(getResources().getString(R.string.lobby_addFriend));
            builderFriends.setCancelable(false);
            builderFriends.setPositiveButton(getResources().getString(R.string.lobby_addFriend), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    final Player player = new Player(username.getText().toString(), "1");
                    //check if string is empty
                    if (!player.getUsername().isEmpty()) {
                        username.setText("");
                    } else {
                        Toast.makeText(Lobby.this, R.string.general_empty_details, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //check if trying to add himself/herself
                    if (player.getUsername().equals(Lobby.this.player.getUsername())) {
                        Toast.makeText(Lobby.this, R.string.lobby_friendlist_cant_add_yourself, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //check if already befriended
                    for (Player p : Collections.synchronizedCollection(Lobby.this.player.getFriends())) {
                        if (p.getUsername().equals(player.getUsername())) {
                            Toast.makeText(Lobby.this, R.string.lobby_friendlist_already_exists, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... params) {
                            DataPacket dp = new DataPacket();
                            dp.setCommand(Commands.ADD_FRIEND);
                            dp.setPlayer(player);
                            mService.sendPacket(dp);
                            return null;
                        }
                    }.execute();
                }
            });
            builderFriends.setNegativeButton(getResources().getString(R.string.lobby_exitFriend), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        }
        dialog = builderFriends.create();
    }

    public void buildExitDialog() {
        if(builder == null) {
            builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.lobby_disconnect));
            builder.setCancelable(false);

            builder.setPositiveButton(getResources().getString(R.string.general_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... params) {
                            DataPacket dp = new DataPacket();
                            dp.setCommand(Commands.DISCONNECT);
                            mService.sendPacket(dp);
                            return null;
                        }
                    }.execute();
                    if (timer != null) {
                        timer.cancel();
                        timer.purge();
                    }
                    Lobby.this.finish();
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
        dialog = builder.create();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
           case R.id.lobby_btn_joinGame:
               btnJoinGame.setEnabled(false);

               //Show join method dialog
               this.dialogJoinMethod.show();
               break;
            case R.id.lobby_btn_options:
                Intent btnOptions = new Intent(Lobby.this,SettingsActivity.class);
                Lobby.this.startActivity(btnOptions);
                break;
            case R.id.lobby_btn_stats:
                Intent btnStats = new Intent(Lobby.this, StatsActivity.class);
                ((GlobalResources)getApplication()).setStatsPlayer(Lobby.this.player);
                Lobby.this.startActivity(btnStats);
                break;
            case R.id.lobby_btn_gameguide:
                Intent btnGameGuide = new Intent(Lobby.this, GameGuide.class);
                Lobby.this.startActivity(btnGameGuide);
                break;
            default:
                break;
        }
    }
}

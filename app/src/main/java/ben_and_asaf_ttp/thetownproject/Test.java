package ben_and_asaf_ttp.thetownproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
import ben_and_asaf_ttp.thetownproject.shared_resources.Game;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class Test extends AppCompatActivity {
    private TextView txtServer;
    private EditText txtSend;
    private EditText txtParams;
    private DataPacket dp;
    private Executor executor;
    private GameService mService;
    private boolean mBound = false;
    private String data;
    private Game game;
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        txtServer = (TextView)findViewById(R.id.txtServer);
        txtServer.setMovementMethod(new ScrollingMovementMethod());
        txtSend = (EditText)findViewById(R.id.txtSend);
        txtParams = (EditText)findViewById(R.id.txtParams);
        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, GameService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(this.getClass().getName(), "OnStop + stop service");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        stopService(new Intent(this, GameService.class));
    }

    public void send(View v){
        this.data = txtSend.getText().toString();
        executor.execute(this.send);
    }

    public void showPlayers(View v){
        if(this.game != null) {
            txtServer.append("\n=====\n" + this.game.getPlayers().toString() + "\n=====\n");
        }
    }

    public void showMe(View v){
        if(this.player != null) {
            txtServer.append(this.player.toString() + "\n");
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
            executor.execute(get);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private Runnable send = new Runnable() {
        @Override
        public void run() {
            DataPacket dp = new DataPacket();
            String [] params = Test.this.txtParams.getText().toString().split(",");
            switch(Test.this.data){
                case "login":
                    dp.setCommand(Commands.LOGIN);
                    dp.setPlayer(new Player(params[0], params[1]));
                    break;
                case "register":
                    dp.setCommand(Commands.REGISTER);
                    dp.setPlayer(new Player(params[0], params[1]));
                    Test.this.player = dp.getPlayer();
                    break;
                case "top10":
                    dp.setCommand(Commands.TOP10);
                    break;
                case "disconnect":
                    dp.setCommand(Commands.DISCONNECT);
                    break;
                case "joingame":
                    dp.setCommand(Commands.READY);
                    dp.setNumber(Integer.decode(params[0]));
                    break;
                case "vote":
                    dp.setCommand(Commands.VOTE);
                    dp.setPlayer(game.getPlayers().get(Integer.decode(params[0])));
                    break;
                case "chat":
                    dp.setCommand(Commands.SEND_MESSAGE);
                    dp.setMessage(params[0]);
                    break;
                case "chatkiller":
                    dp.setCommand(Commands.SEND_MESSAGE_KILLER);
                    dp.setMessage(params[0]);
                    break;
                case "chatdead":
                    dp.setCommand(Commands.SEND_MESSAGE_DEAD);
                    dp.setMessage(params[0]);
                    break;
                case "doaction":
                    Test.this.player.getRole().action(dp);
                    dp.setPlayer(game.getPlayers().get(Integer.decode(params[0])));
                    break;
                case "exit":
                    Test.this.finish();
                    return;
                case "shutdown":
                    dp.setCommand(Commands.SERVER_SHUTDOWN);
                    break;
                default:
                    dp = null;
                    break;
            }
            if(dp != null) {
                Test.this.mService.sendPacket(dp);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Test.this.txtSend.setText("");
                    Test.this.txtParams.setText("");
                    Test.this.txtSend.requestFocus();
                }
            });
        }
    };

    private Runnable get = new Runnable() {
        @Override
        public void run() {
            while( (Test.this.dp = Test.this.mService.getPacket()) != null) {
                switch(Test.this.dp.getCommand()) {
                    case LOGIN:
                        Test.this.player = Test.this.dp.getPlayer();

                        //problem with GSON because of circular reference requires sending this separately
                        Test.this.player.setGameHistory(Test.this.dp.getGames());
                        break;
                    case REFRESH_PLAYERS:
                    case PLAYER_JOINED:
                        Test.this.game.setPlayers(Test.this.dp.getPlayers());
                        Test.this.player = Test.this.game.getPlayers().get(Test.this.game.getPlayers().indexOf(Test.this.player));
                        break;
                    case PLAYER_LEFT:
                        Test.this.game.getPlayers().remove(Test.this.dp.getPlayer());
                        break;
                    case OK:
                        Test.this.game = Test.this.dp.getGame();
                        break;
                    case DISCONNECT:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Test.this, "Disconnect", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case READY:
                        Test.this.player = Test.this.dp.getPlayers().get(Test.this.dp.getPlayers().indexOf(Test.this.player));
                        Log.i(this.getClass().getName(), "Role is: " + Test.this.player.getRole().toString());
                        break;
                    case SERVER_SHUTDOWN:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Test.this, "Server Shutdown", Toast.LENGTH_SHORT).show();
                            }
                        });
                        ClientConnection.getConnection().closeSocket();
                        break;
                }

                synchronized(updateLog) {
                    try {
                        runOnUiThread(updateLog);
                        updateLog.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private Runnable updateLog = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                txtServer.append("\n=====\n" + Test.this.dp.toString() + "\n=====\n");
                this.notify();
            }
        }
    };
}

package ben_and_asaf_ttp.thetownproject;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;

import java.io.IOException;
import java.util.concurrent.Executor;

import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;
import ben_and_asaf_ttp.thetownproject.shared_resources.Game;
import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GameService extends Service {
    public static boolean isRunning = false;
    private Executor executor;
    private Runnable send;
    private DataPacket dpSend;
    private DataPacket dpRecieve;
    private IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        GameService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GameService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public DataPacket getPacket() throws IOException, ClassNotFoundException {
        GameService.this.dpRecieve = ClientConnection.getConnection().receiveDataPacket();
        return dpRecieve;
    }

    public void sendPacket(DataPacket dpSend){
        this.dpSend = dpSend;
        executor.execute(send);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        };

        send = new Runnable() {
            @Override
            public void run() {
                try {
                    ClientConnection.getConnection().sendDataPacket(GameService.this.dpSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        isRunning = true;
    }
}

package ben_and_asaf_ttp.thetownproject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;

/**
 * An {@link Service} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GameService extends Service {
    public static boolean isRunning = false;
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

    public DataPacket getPacket(){
        return ClientConnection.getConnection().receiveDataPacket();
    }

    public void sendPacket(DataPacket dpSend){
        ClientConnection.getConnection().sendDataPacket(dpSend);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
    }
}

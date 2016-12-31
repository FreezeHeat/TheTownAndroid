package ben_and_asaf_ttp.thetownproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShutdownReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(this.getClass().getName(), "*****PHONE SHUTDOWN*****");
        ClientConnection.getConnection().closeSocket();
    }
}

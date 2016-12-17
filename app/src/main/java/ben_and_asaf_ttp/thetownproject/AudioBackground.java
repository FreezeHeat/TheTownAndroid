package ben_and_asaf_ttp.thetownproject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class AudioBackground extends Service implements MediaPlayer.OnPreparedListener{
    private static final String bgType = "BG";
    private static final String fxType = "FX";
    private MediaPlayer bg = null;
    private MediaPlayer fx = null;

    public AudioBackground() {}

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getStringExtra("type")){
            case bgType:

                break;
            case fxType:

                break;
        }
//        mPlayer.create(this, R.raw.bg);
//        mPlayer.setLooping(true);
//        mPlayer.setVolume(0.5f, 0.5f);
//        mPlayer.setOnPreparedListener(this);
//        mPlayer.prepareAsync();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bg.release();
        bg = null;
        fx.release();
        fx = null;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mPlayer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

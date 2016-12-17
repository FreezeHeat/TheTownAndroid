package ben_and_asaf_ttp.thetownproject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class AudioBackground extends Service implements MediaPlayer.OnPreparedListener{
    private static final String play = "ben_and_asaf_ttp.thetownproject.PLAY";
    private static final String pause = "ben_and_asaf_ttp.thetownproject.PAUSE";
    private MediaPlayer mPlayer = null;

    public AudioBackground() {}

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPlayer.create(this, R.raw.bg);
        mPlayer.setLooping(true);
        mPlayer.setVolume(0.5f, 0.5f);
        mPlayer.setOnPreparedListener(this);
        mPlayer.prepareAsync();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.release();
        mPlayer = null;
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

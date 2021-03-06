package ben_and_asaf_ttp.thetownproject;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.util.concurrent.Executor;

public class AudioBackground extends Service implements MediaPlayer.OnPreparedListener{
    private static final String bgType = "BG";
    private static final String fxType = "FX";
    private static int sound = 0;
    private static String type = null;
    private static MediaPlayer bg = null;
    private static MediaPlayer fx = null;
    private static boolean isPlaying;
    SharedPreferences myPrefs;
    private Executor executor;
    private boolean sameSound;

    public AudioBackground() {}

    public static synchronized boolean isPlaying() {
        return isPlaying;
    }

    public static synchronized  MediaPlayer getBg() {
        return bg;
    }

    public static synchronized MediaPlayer getFx() {
        return fx;
    }

    private synchronized void setBg(MediaPlayer bg) {
        AudioBackground.bg = bg;
    }

    private synchronized void setFx(MediaPlayer fx) {
        AudioBackground.fx = fx;
    }

    private synchronized String getType(){
        return type;
    }

    private synchronized void setType(String type){
        AudioBackground.type = type;
    }

    private synchronized int getSound(){
        return sound;
    }

    private synchronized void setSound(int sound){
        AudioBackground.sound = sound;
    }

    public synchronized boolean isSameSound() {
        return sameSound;
    }

    public synchronized void setSameSound(boolean sameSound) {
        this.sameSound = sameSound;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setType(intent.getStringExtra("type"));
        setSound(intent.getIntExtra("sound", -1));
        executor.execute(new Runnable() {
            @Override
            public void run() {

                switch (getType()){
                    case bgType:
                        final float volBg = myPrefs.getFloat("bgVolume", 1.0f);
                        if( (!isPlaying) && volBg > 0.0f ) {
                            if (getBg() == null) {
                                setBg(MediaPlayer.create(AudioBackground.this, getSound()));
                                getBg().setVolume(volBg, volBg);
                                getBg().setLooping(true);
                                getBg().setOnPreparedListener(AudioBackground.this);
                            }
                        }else{
                            if (volBg <= 0.0f || isPlaying()) {
                                //User wants NO background music
                                isPlaying = false;
                                if (getBg() != null) {
                                    getBg().release();
                                    setBg(null);
                                }
                            }
                        }
                        break;
                    case fxType:
                        final float volFx = myPrefs.getFloat("fxVolume", 1.0f);
                        if(volFx > 0.0f) {
                            if(!isSameSound()) {
                                setFx(MediaPlayer.create(AudioBackground.this, getSound()));
                                getFx().setVolume(volFx, volFx);
                                getFx().setOnPreparedListener(AudioBackground.this);
                            }else{
                                getFx().start();
                            }
                        }else{

                            //User wants NO sound effects
                            if (getFx() != null) {
                                getFx().release();
                                setFx(null);
                            }
                        }
                        break;
                }
            }
        });
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(getBg() != null) {
            isPlaying = false;
            getBg().release();
            setBg(null);
        }

        if(getFx() != null) {
            getFx().release();
            setFx(null);
        }
    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

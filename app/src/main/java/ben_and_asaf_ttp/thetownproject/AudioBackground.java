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
    private MediaPlayer bg = null;
    private MediaPlayer fx = null;
    SharedPreferences myPrefs;
    private Executor executor;

    public AudioBackground() {}

    public MediaPlayer getBg() {
        return bg;
    }

    public void setBg(MediaPlayer bg) {
        this.bg = bg;
    }

    public MediaPlayer getFx() {
        return fx;
    }

    public void setFx(MediaPlayer fx) {
        this.fx = fx;
    }

    private synchronized String getType(){
        return this.type;
    }

    private synchronized void setType(String type){
        this.type = type;
    }

    private synchronized int getSound(){
        return this.sound;
    }

    private synchronized void setSound(int sound){
        this.sound = sound;
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
                        if(getBg() == null) {
                            setBg(getBg().create(AudioBackground.this, getSound()));
                            getBg().setVolume(volBg, volBg);
                            getBg().setLooping(true);
                            getBg().setOnPreparedListener(AudioBackground.this);
                        }else{
                            if(volBg > 0.0f) {
                                //User wants NO background music
                                getBg().release();
                                setBg(null);
                            }
                        }
                        break;
                    case fxType:
                        final float volFx = myPrefs.getFloat("fxVolume", 1.0f);
                        if(volFx > 0.0f) {
                            setFx(getFx().create(AudioBackground.this, getSound()));
                            getFx().setVolume(volFx, volFx);
                            getFx().setOnPreparedListener(AudioBackground.this);
                        }else{
                            //User wants NO sound effects
                            if(getFx() != null){
                                getFx().release();
                                setFx(null);
                            }
                        }
                        break;
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(getBg() != null) {
            getBg().release();
            setBg(null);
        }

        if(getFx() != null) {
            getFx().release();
            setFx(null);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                switch (getType()){
                    case bgType:
                        getBg().start();
                        break;
                    case fxType:
                        getFx().start();
                        break;
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

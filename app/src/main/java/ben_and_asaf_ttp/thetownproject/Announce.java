package ben_and_asaf_ttp.thetownproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by user on 15/12/2016.
 */

public class Announce extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_announce);

        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                Announce.this.finish();
            }
        }, 5000);

    }
}

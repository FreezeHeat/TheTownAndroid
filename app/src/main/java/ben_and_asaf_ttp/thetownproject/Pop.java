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
 * Created by user on 26/11/2016.
 */

public class Pop extends Activity{

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pop);


        webView = (WebView)findViewById(R.id.webView);
        webView.loadUrl(this.getIntent().getStringExtra("animation"));


        //webView.loadUrl("file:///android_asset/moon.html");
        //webView.loadUrl("file:///android_asset/execute.html");
        //webView.loadUrl("file:///android_asset/sun.html");
        //webView.loadUrl("file:///android_asset/murder.html");

        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                Pop.this.finish();
            }
        }, 6500);

    }
}

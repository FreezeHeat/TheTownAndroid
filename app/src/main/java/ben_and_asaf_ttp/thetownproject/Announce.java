package ben_and_asaf_ttp.thetownproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class Announce extends Activity {
    TextView txtMsg;
    ImageView imgvIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_announce);

        txtMsg = (TextView)findViewById(R.id.announce_txt_gameAnnounce);
        imgvIcon = (ImageView)findViewById(R.id.announce_imgv_icon);

        txtMsg.setText(this.getIntent().getStringExtra("msg"));
        final int icon = this.getIntent().getIntExtra("icon", -1);
        if(icon != -1){
            imgvIcon.setImageResource(icon);
            imgvIcon.setVisibility(View.VISIBLE);
        }

        //Play sound effect
        final int sfx = this.getIntent().getIntExtra("sfx", -1);
        if(sfx != -1){
            final Intent intent = new Intent(Announce.this, AudioBackground.class);
            intent.putExtra("type", "FX");
            intent.putExtra("sound", sfx);
            startService(intent);
        }

        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                setResult(RESULT_OK, getIntent());
                Announce.this.finish();
            }
        }, 4000);
    }
}

package ben_and_asaf_ttp.thetownproject;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    TextView txtSplashScreen;
    ProgressBar pbSplashScreen;
    ImageView imgSplashScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        txtSplashScreen = (TextView)findViewById(R.id.txtSplashScreen);
        pbSplashScreen = (ProgressBar)findViewById(R.id.pbSplashScreen);
        imgSplashScreen = (ImageView)findViewById(R.id.imgSplashScreen);

        MyLoaderTask myTask = new MyLoaderTask();
        myTask.execute();
    }

    class MyLoaderTask extends AsyncTask<Void, Integer, Boolean>
    {

        @Override
        protected void onPreExecute() {
            pbSplashScreen.setVisibility(View.VISIBLE);
            txtSplashScreen.setText("0 %");
        }

        // this method runs on UI - so Toast is ok here
        @Override
        protected void onProgressUpdate(Integer... values) {
            //Toast.makeText(SplashActivity.this, "Progress round " + values[0], Toast.LENGTH_SHORT).show();
            pbSplashScreen.setProgress(values[0]);
            txtSplashScreen.setText(values[0] + " %");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            for (int i = 0; i < 5; i++){
                try {
                    Thread.sleep(1000);
                    publishProgress(i*20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        // this runs on UI thread
        @Override
        protected void onPostExecute(Boolean result) {
            if(result)
            {
                pbSplashScreen.setProgress(100);
                txtSplashScreen.setText("100 %");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();   // finish the activity of the splash so it will not be in the history
                Intent myIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(myIntent);
            }
        }
    }
}

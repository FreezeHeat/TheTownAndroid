package ben_and_asaf_ttp.thetownproject;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    private TextView txtSplashScreen;
    private ProgressBar pbSplashScreen;
    private ImageView imgSplashScreen;

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

    class MyLoaderTask extends AsyncTask<Void, String, Boolean>
    {

        @Override
        protected void onPreExecute() {
            pbSplashScreen.setVisibility(View.VISIBLE);
            txtSplashScreen.setText("0 %");
        }

        // this method runs on UI - so Toast is ok here
        @Override
        protected void onProgressUpdate(String... values) {

            //failed to connect to socket
            if(values[0].equals("-1")){
                txtSplashScreen.setText(values[1]);
            }else {
                pbSplashScreen.setProgress(Integer.decode(values[0]));
                txtSplashScreen.setText(values[1]);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            publishProgress("0", getResources().getString(R.string.splash_socket_connection));
            ClientConnection.getConnection().startConnection();
            if(!ClientConnection.getConnection().isOnline()){
                publishProgress("-1", getResources().getString(R.string.splash_socket_failed));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }else{
                publishProgress("50", getResources().getString(R.string.splash_socket_success));
                try {
                    Thread.sleep(1000);
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
                txtSplashScreen.setText("Successful - starting the game");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();   // finish the activity of the splash so it will not be in the history
                Intent myIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(myIntent);
            }
            else{

                //in case starting up the socket failed
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e(this.getClass().getName(), "Error connecting to socket");
                finish();
            }
        }
    }
}

package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.io.IOException;

public class SplashActivity extends AppCompatActivity {

    private TextView txtSplashScreen;
    private ProgressBar pbSplashScreen;
    private ImageView imgSplashScreen;
    private AlertDialog.Builder builder;

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
            txtSplashScreen.setText(getResources().getString(R.string.splash_socket_connection));
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
            try {
                ClientConnection.getConnection().startConnection();
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress("-1", getResources().getString(R.string.splash_socket_failed));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                return false;
            }

            publishProgress("50", getResources().getString(R.string.splash_socket_success));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                    Thread.sleep(500);
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
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e(this.getClass().getName(), "Error connecting to socket");
                buildExitDialog();
                builder.show();
            }
        }
    }

    public void buildExitDialog() {
        if(builder == null) {
            builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getText(R.string.general_connection_problem));
            builder.setCancelable(false);
            builder.setPositiveButton(getResources().getString(R.string.general_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SplashActivity.this.finish();
                    try {
                        if(ClientConnection.getConnection().getOutput() != null) {
                            ClientConnection.getConnection().exit();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}

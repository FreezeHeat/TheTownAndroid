package ben_and_asaf_ttp.thetownproject;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

public class SplashActivity extends AppCompatActivity {

    private TextView txtSplashScreen;
    private ProgressBar pbSplashScreen;
    private AlertDialog.Builder builder;
    private final String server = "35.156.51.88";
    private final String port = "55555";
    private final String webService = "http://35.156.51.88:8080/TTWS";
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        txtSplashScreen = (TextView)findViewById(R.id.txtSplashScreen);
        pbSplashScreen = (ProgressBar)findViewById(R.id.pbSplashScreen);
        final ImageView imgSplashScreen = (ImageView) findViewById(R.id.imgSplashScreen);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
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
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                final String serverIp = sp.getString("serverIp", server);
                final String serverPort = sp.getString("serverPort", port);

                //if the defaults are set
                if(serverIp.equals(server) && serverPort.equals(port)){
                    sp.edit().putString("serverWs", webService).apply();
                }
                ClientConnection.getConnection().startConnection(serverIp, Integer.decode(serverPort));
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress("-1", getResources().getString(R.string.splash_socket_failed));
                return false;
            }
            publishProgress("50", getResources().getString(R.string.splash_socket_success));

            try {
                Thread.sleep(2500);
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
                txtSplashScreen.setText(getResources().getString(R.string.splash_socket_startGame));

                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                finish();   // finish the activity of the splash so it will not be in the history
                //Intent myIntent = new Intent(SplashActivity.this, MainActivity.class);
                Intent myIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(myIntent);
            }
            else{

                //in case starting up the socket failed
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
                    buildServerDialog();
                    builder.show();
                }
            });
        }
    }

    public void buildServerDialog(){
        if(builder == null){
            builder = new AlertDialog.Builder(this);
        }
        builder.setMessage(R.string.splash_dialog_explanation);
        final LinearLayout layout = new LinearLayout(this);
        final EditText ip = new EditText(this);
        final EditText port = new EditText(this);
        final EditText ws = new EditText(this);
        ip.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        port.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ws.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ip.setHint(R.string.splash_dialog_hint_ip);
        port.setHint(R.string.splash_dialog_hint_port);
        ws.setHint(R.string.splash_dialog_hint_ws);

        final SharedPreferences.Editor editor = sp.edit();
        ip.setText(sp.getString("serverIp", SplashActivity.this.server));
        port.setText(sp.getString("serverPort", SplashActivity.this.port));
        ws.setText(sp.getString("serverWs", SplashActivity.this.webService));

        layout.addView(ip);
        layout.addView(port);
        layout.addView(ws);
        layout.setGravity(View.TEXT_ALIGNMENT_CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);

        builder.setView(layout);

        builder.setPositiveButton(getString(R.string.general_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                editor.putString("serverIp", ip.getText().toString());
                editor.putString("serverPort", port.getText().toString());
                editor.putString("serverWs", ws.getText().toString());
                editor.apply();

                SplashActivity.this.finish();
                if(ClientConnection.getConnection().getOutput() != null) {
                    ClientConnection.getConnection().closeSocket();
                }
            }
        });

        builder.setNegativeButton("Restore to default", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editor.putString("serverIp", SplashActivity.this.server);
                editor.putString("serverPort", SplashActivity.this.port);
                editor.putString("serverWs", SplashActivity.this.webService);
                editor.apply();

                SplashActivity.this.finish();
                if(ClientConnection.getConnection().getOutput() != null) {
                    ClientConnection.getConnection().closeSocket();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {}
}

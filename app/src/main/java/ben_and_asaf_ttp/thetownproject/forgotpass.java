package ben_and_asaf_ttp.thetownproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Forgotpass extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpass);
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

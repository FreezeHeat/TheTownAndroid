package ben_and_asaf_ttp.thetownproject;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by user on 13/08/2016.
 */
public class Lobby extends AppCompatActivity {

String[] friendsListName;
    String[] friendsListStatus;
    int images[];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        Resources res = getResources();
        friendsListName = res.getStringArray(R.array.friends_list_name);
        friendsListStatus = res.getStringArray(R.array.friends_list_status);

    }
}

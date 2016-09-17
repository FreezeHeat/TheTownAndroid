package ben_and_asaf_ttp.thetownproject;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class Lobby extends AppCompatActivity{

    String[] friendsListName = {"Ben","Asaf"};
    int images[];
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, friendsListName));
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

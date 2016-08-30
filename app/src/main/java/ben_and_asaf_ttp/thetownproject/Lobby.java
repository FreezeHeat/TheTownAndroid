package ben_and_asaf_ttp.thetownproject;

import android.app.FragmentManager;
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


public class Lobby extends AppCompatActivity implements DrawerLayout.DrawerListener{

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
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.friends_list_single_row, friendsListName));
        // Resources res = getResources();
        //friendsListName = res.getStringArray(R.array.friends_list_name);
        //friendsListStatus = res.getStringArray(R.array.friends_list_status);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(Lobby.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();
            }
        });    }
    private void addDrawerItems() {
        String[] osArray = {"Android", "IOS", "Windows"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
    }


    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {

        Toast.makeText(this,"hello asafi, good job!",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        Toast.makeText(this,"closed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

}

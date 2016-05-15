
package ben_and_asaf_ttp.thetownproject;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ShowGames extends AppCompatActivity implements AdapterView.OnItemClickListener {

    static final int MENU_DELETE = 1;
    static final int MENU_EDIT = 2;

    ArrayList<Game> games = new ArrayList<Game>();
    GameAdapter gameAdapter;
    ListView myListView;
    AdapterView.AdapterContextMenuInfo info;


    public void refreshCrap(){


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_games);



        myListView = (ListView)findViewById(R.id.MyGameList);

        // build the adapter - to point and use the cities list of Strings and use the android layout for each line in the list
        gameAdapter = new GameAdapter(this, R.layout.single_game, games);

        // associate the ListView to use the data from the adapter
        myListView.setAdapter(gameAdapter);

        //Click listener and ContextMenu registration
        myListView.setOnItemClickListener(this);
        registerForContextMenu(myListView);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        menu.setHeaderTitle("Game Menu");
        menu.add(Menu.NONE, MENU_DELETE, 1, "Delete");
        menu.add(Menu.NONE, MENU_EDIT, 2, "Edit");
        info = (AdapterView.AdapterContextMenuInfo)menuInfo;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Game c = (Game) myListView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case MENU_DELETE:

                gameAdapter.remove(c);
                games.remove(c);
                break;
            case MENU_EDIT:


                break;
        }

        return true;
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Game game = (Game) parent.getItemAtPosition(position);
        // Intent result = getIntent();
        //   result.putExtra("city", city);
        // setResult(RESULT_OK, result);
        finish();
    }

    public class GameAdapter extends ArrayAdapter<Game> {
        public GameAdapter(Context context, int resource, List<Game> objects) {
            super(context, resource, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Game game = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.single_game,
                        parent,
                        false);
            }

            TextView gameDescription = (TextView) convertView.findViewById(R.id.txtGameDescription);
            TextView gameNumPlayers = (TextView) convertView.findViewById(R.id.txtNumPlayers);

            gameDescription.setText(game.getDescription());
            gameNumPlayers.setText(game.getNumPlayers());

            return convertView;
        }
    }
}

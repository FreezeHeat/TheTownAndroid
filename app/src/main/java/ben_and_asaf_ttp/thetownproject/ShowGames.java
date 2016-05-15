
package ben_and_asaf_ttp.thetownproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ben_and_asaf_ttp.thetownproject.DB.DBHandler;

public class ShowGames extends AppCompatActivity implements AdapterView.OnItemClickListener {

    static final int MENU_DELETE = 1;
    static final int MENU_EDIT = 2;

    ArrayList<Game> games;
    GameAdapter gameAdapter;
    ListView myListView;
    AdapterView.AdapterContextMenuInfo info;
    DBHandler dbHandler = new DBHandler(this);


    public void refreshCrap(){

        //clean list if not empty
        if(games != null){
            games.clear();
        }

        games = dbHandler.getAllGames();

        //clean the adapter view
        gameAdapter.clear();
        gameAdapter.addAll(games);

        // refresh list view
        myListView.setAdapter(gameAdapter);
/////check
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_games);

        myListView = (ListView)findViewById(R.id.MyGameList);

        //get all games from the DB
        games = dbHandler.getAllGames();

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
                // get the item and delete it from the database
                Game toDelete = (Game)gameAdapter.getItem(info.position);
                if(dbHandler.deleteGame(toDelete)){
                    Toast.makeText(this, "Game deleted", Toast.LENGTH_SHORT).show();
                    refreshCrap();
                }
                else{
                    Toast.makeText(this, "Game wasn't deleted", Toast.LENGTH_SHORT).show();
                }
                break;
            case MENU_EDIT:
                Game toEdit = (Game)gameAdapter.getItem(info.position);
                showGameEditMenu(toEdit);
                break;
        }

        return true;
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Game game = (Game) parent.getItemAtPosition(position);
        Toast.makeText(this, "clicked " + game.getDescription(), Toast.LENGTH_SHORT).show();
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
            gameNumPlayers.setText("" + game.getNumPlayers());

            return convertView;
        }
    }

    public void showGameEditMenu(final Game toEdit){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit game description");

    // Set up the input
        final EditText input = new EditText(this);
    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

    // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHandler.editGame(input.getText().toString(), toEdit);
                refreshCrap();
                Toast.makeText(ShowGames.this, "Edited the game", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}

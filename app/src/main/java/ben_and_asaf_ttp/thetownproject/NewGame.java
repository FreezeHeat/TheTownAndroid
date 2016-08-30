package ben_and_asaf_ttp.thetownproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import ben_and_asaf_ttp.thetownproject.DB.DBHandler;

public class NewGame extends AppCompatActivity {
    DBHandler dbHandler;
    EditText gameDescription;
    EditText gameNumPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        dbHandler = new DBHandler(this);
        gameDescription = (EditText)findViewById(R.id.editGameDescription);
        gameNumPlayers = (EditText)findViewById((R.id.editNumPlayers));
    }
/*
    public void addGame(View v)
    {
        Game game = new Game();
        game.setDescription(gameDescription.getText().toString());
        game.setNumPlayers(Integer.parseInt(gameNumPlayers.getText().toString()));

        if(dbHandler.addGame(game))
            Toast.makeText(this, "Game was added succesfuly", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Game wasn't added", Toast.LENGTH_SHORT).show();

    }
    */
}

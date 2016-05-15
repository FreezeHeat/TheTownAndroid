package ben_and_asaf_ttp.thetownproject.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import ben_and_asaf_ttp.thetownproject.Game;

public class DBHandler {

    private MySQLiteHelper dbHelper;

    public DBHandler(Context context)
    {
        dbHelper = new MySQLiteHelper(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION);
    }

    // returns true/false if the addition was successful
    public boolean addGame(Game newGame)
    {

        // this opens the connection to the DB
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues columnValues = new ContentValues();
        columnValues.put(DBConstants.GAME_DESCRIPTION, newGame.getDescription());
        columnValues.put(DBConstants.GAME_NUM_PLAYERS, newGame.getNumPlayers());

        long result = db.insert(DBConstants.GAME_TABLE_NAME, null, columnValues);

        db.close();

        // when result is -1 it means the insert has failed, so when NOT -1 it was successful
        return (result != -1);
    }

    public boolean deleteGame(Game deleteGame){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String [] args = {deleteGame.getDescription()};
        int result =
                db.delete(DBConstants.GAME_TABLE_NAME,
                        "GAME_DESCRIPTION LIKE ?",
                        args);

        return (result >= 1 );
    }

    public boolean editGame(String newValue, Game editGame){

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues newValues = new ContentValues();
        newValues.put("GAME_DESCRIPTION", newValue);

        String [] args = {editGame.getDescription()};
        int result =
                db.update(DBConstants.GAME_TABLE_NAME,
                        newValues,
                        "GAME_DESCRIPTION LIKE ?",
                        args);

       return (result >= 1 );
    }

    public ArrayList<Game> getAllGames()
    {
        ArrayList<Game> gamesList = new ArrayList<Game>();
        // this opens the connection to the DB
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // select * from BOOKS table
        Cursor gameCursor = db.query(DBConstants.GAME_TABLE_NAME, null, null, null, null, null, null);
        // each round in the loop is a record in the DB
        while(gameCursor.moveToNext()) {
            String gameDescription = gameCursor.getString(0);
            int gameNumPlayers= gameCursor.getInt(1);

            Game g = new Game(gameDescription, gameNumPlayers);
            gamesList.add(g);
        }

        return gamesList;

    }

}

package ben_and_asaf_ttp.thetownproject.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ben_and_asaf_ttp.thetownproject.shared_resources.Player;

public class DBHandler {

    private MySQLiteHelper dbHelper;

    public DBHandler(Context context)
    {
        dbHelper = new MySQLiteHelper(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION);
    }

    public Player getPlayer(Player p){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //expected columns from which to return information from
        String columns[] = {DBConstants.PLAYER_USERNAME, DBConstants.PLAYER_PASSWORD};

        //Values for the WHERE query below
        String [] args = {p.getUsername(), p.getPassword()};

        Cursor result =
                db.query(DBConstants.PLAYER_TABLE_NAME,
                        columns, //which information to extract
                        DBConstants.PLAYER_USERNAME + " LIKE ? AND " + //where query
                        DBConstants.PLAYER_PASSWORD + " LIKE ?",
                        args, //values for the query above
                        null, //sql GROUP
                        null, //sql FILTER BY
                        null//sql ORDER BY
                         );

        //if there were no results
        if(result.getCount() <= 0){
            return null;
        }

        //get first row result
        result.moveToFirst();

        //extract information and return it as Player
        p = new Player(result.getString(result.getColumnIndex(DBConstants.PLAYER_USERNAME)),
                result.getString(result.getColumnIndex(DBConstants.PLAYER_PASSWORD)));

        //close cursor
        result.close();

        return p;
    }

    // returns true/false if the addition was successful
    public boolean savePlayer(Player p)
        {

        // this opens the connection to the DB
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues columnValues = new ContentValues();
        columnValues.put(DBConstants.PLAYER_USERNAME, p.getUsername());
        columnValues.put(DBConstants.PLAYER_PASSWORD, p.getPassword());

        long result = db.insert(DBConstants.PLAYER_TABLE_NAME, null, columnValues);

        db.close();

        // when result is -1 it means the insert has failed, so when NOT -1 it was successful
        return (result != -1);
    }

    public boolean deletePlayer(Player p){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String [] args = {p.getUsername()};
        int result =
                db.delete(DBConstants.PLAYER_TABLE_NAME,
                        DBConstants.PLAYER_USERNAME + " LIKE ?",
                        args);

        return (result >= 1 );
    }

    public boolean editPlayer(Player p){

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues newValues = new ContentValues();
       newValues.put(DBConstants.PLAYER_USERNAME, p.getUsername());
       newValues.put(DBConstants.PLAYER_PASSWORD, p.getPassword());

        String [] args = {p.getUsername()};
        int result =
                db.update(DBConstants.PLAYER_TABLE_NAME,
                        newValues,
                        DBConstants.PLAYER_USERNAME + " LIKE ?",
                        args);

       return (result >= 1 );
    }
}

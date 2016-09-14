package ben_and_asaf_ttp.thetownproject.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBHandler {

    private MySQLiteHelper dbHelper;

    public DBHandler(Context context)
    {
        dbHelper = new MySQLiteHelper(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION);
    }

    // returns true/false if the addition was successful
    public boolean savePlayer(/*Player class */)
    {

        // this opens the connection to the DB
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues columnValues = new ContentValues();
       // columnValues.put(DBConstants.PLAYER_USERNAME, /*Player class - username*/);
        //columnValues.put(DBConstants.PLAYER_PASSWORD, /*Player class - password*/);

        long result = db.insert(DBConstants.PLAYER_TABLE_NAME, null, columnValues);

        db.close();

        // when result is -1 it means the insert has failed, so when NOT -1 it was successful
        return (result != -1);
    }

    public boolean deletePlayer(/*Player class */){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String [] args = {/*Player class - username*/};
        int result =
                db.delete(DBConstants.PLAYER_TABLE_NAME,
                        DBConstants.PLAYER_USERNAME + " LIKE ?",
                        args);

        return (result >= 1 );
    }

    public boolean editPlayer(/*Player class */){

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues newValues = new ContentValues();
       // newValues.put(DBConstants.PLAYER_USERNAME, /*Player class - username*/);
       // newValues.put(DBConstants.PLAYER_PASSWORD, /*Player class - password*/);

        String [] args = {/*Player class - username*/};
        int result =
                db.update(DBConstants.PLAYER_TABLE_NAME,
                        newValues,
                        DBConstants.PLAYER_USERNAME + " LIKE ?",
                        args);

       return (result >= 1 );
    }
}

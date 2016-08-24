package ben_and_asaf_ttp.thetownproject.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ben_and_asaf_ttp.thetownproject.DB.DBConstants;

public class MySQLiteHelper extends SQLiteOpenHelper {


    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // execute all create statements
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DBConstants.PLAYER_TABLE_NAME +
                "( " + DBConstants.PLAYER_USERNAME + " TEXT, " +
                DBConstants.PLAYER_PASSWORD + " TEXT)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



}

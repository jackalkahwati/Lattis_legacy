package cc.skylock.skylock.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Velo Labs Android on 29-01-2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context, Dbfunction.SKYLOCK_DATABASE_NAME, null, Dbfunction.DATABASE_VERSION);
        // TODO Auto-generated constructor stub
        Log.i("DataBase : ", "DatabaseHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(Dbfunction.SKYLOCK_USER_TABLE);
        db.execSQL(Dbfunction.SKYLOCK_LOCKS_TABLE);
        Log.i("DataBase : ", "db created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

//                Log.e("db ", "upgrade version"+oldVersion+"::"+newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + Dbfunction.SKYLOCK_USER_TABLE);
        onCreate(db);
        Log.i("DataBase : ", "onUpgrade db created");


    }
}

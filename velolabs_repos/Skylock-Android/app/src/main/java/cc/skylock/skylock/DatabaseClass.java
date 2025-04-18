package cc.skylock.skylock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

/**
 * Created by AlexVijayRaj on 8/20/2015.
 */
public class DatabaseClass {

    public static final String KEY_ROW_ID = "_id";
    public static final String KEY_ID = "ID";
    public static final String KEY_NAME = "persons_name";
    public static final String KEY_MAC_ADDRESS = "locks_macaddress";
    public static final String KEY_LAT = "locks_lat";
    public static final String KEY_LNG = "locks_lng";

    private static final String DATABASE_NAME = "LocalDb1";
    private static final String DATABASE_TABLE = "peopleTable1";
    private static final int DATABASE_VERSION = 1;

    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;

    public String getData() {
        String[] columns = new String[] {KEY_ROW_ID, KEY_ID, KEY_NAME, KEY_MAC_ADDRESS, KEY_LAT,KEY_LNG};
        Cursor c = ourDatabase.query(DATABASE_TABLE,columns,null,null,null,null,null);
        String result = "";

        int iID = c.getColumnIndex(KEY_ID);
        int iRow = c.getColumnIndex(KEY_ROW_ID);
        int iName = c.getColumnIndex(KEY_NAME);
        int iMacAddress = c.getColumnIndex(KEY_MAC_ADDRESS);
        int iLat = c.getColumnIndex(KEY_LAT);
        int iLng = c.getColumnIndex(KEY_LNG);


        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            result = result + c.getString(iRow) +" " + c.getString(iID) + " " + c.getString(iName) + " " + c.getString(iMacAddress) + " " + c.getString(iLat) + " " + c.getString(iLng) +"\n";
        }

        return result;
    }

    public String getId(long l) {
        String[] columns = new String[]{KEY_ROW_ID, KEY_ID, KEY_NAME, KEY_MAC_ADDRESS, KEY_LAT,KEY_LNG};
        Cursor c = ourDatabase.query(DATABASE_TABLE,columns,KEY_ROW_ID + "=" + l, null, null, null, null);
        if(c != null){
            c.moveToFirst();
            String id = c.getString(1);
            return id;
        }
        return null;

    }

    public String getName(long l) {

        String[] columns = new String[]{KEY_ROW_ID, KEY_ID, KEY_NAME, KEY_MAC_ADDRESS, KEY_LAT,KEY_LNG};
        Cursor c = ourDatabase.query(DATABASE_TABLE,columns,KEY_ROW_ID + "=" + l, null, null, null, null);
        if(c != null){
            c.moveToFirst();
            String name = c.getString(2);
            return name;
        }
        return null;
    }

    public String getMacAddress(long l) {

        String[] columns = new String[]{KEY_ROW_ID, KEY_ID, KEY_NAME, KEY_MAC_ADDRESS, KEY_LAT,KEY_LNG};
        Cursor c = ourDatabase.query(DATABASE_TABLE,columns,KEY_ROW_ID + "=" + l, null, null, null, null);
        if(c != null){
            c.moveToFirst();
            String macaddress = c.getString(3);
            return macaddress;
        }
        return null;

    }

    public String getLat(long l) {

        String[] columns = new String[]{KEY_ROW_ID, KEY_ID, KEY_NAME, KEY_MAC_ADDRESS, KEY_LAT,KEY_LNG};
        Cursor c = ourDatabase.query(DATABASE_TABLE,columns,KEY_ROW_ID + "=" + l, null, null, null, null);
        if(c != null){
            c.moveToFirst();
            String Lat = c.getString(4);
            return Lat;
        }
        return null;

    }

    public String getLng(long l) {

        String[] columns = new String[]{KEY_ROW_ID, KEY_ID, KEY_NAME, KEY_MAC_ADDRESS, KEY_LAT,KEY_LNG};
        Cursor c = ourDatabase.query(DATABASE_TABLE,columns,KEY_ROW_ID + "=" + l, null, null, null, null);
        if(c != null){
            c.moveToFirst();
            String Lng = c.getString(5);
            return Lng;
        }
        return null;

    }



    public void updateEntry(long lRow, String mID, String mName, String mMacAddress, String Lat, String Lng) {

        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(KEY_ID,mID);
        cvUpdate.put(KEY_NAME,mName);
        cvUpdate.put(KEY_MAC_ADDRESS,mMacAddress);
        cvUpdate.put(KEY_LAT,Lat);
        cvUpdate.put(KEY_LNG,Lng);
        ourDatabase.update(DATABASE_TABLE, cvUpdate, KEY_ROW_ID + "=" + lRow, null);

    }

    public void deleteEntry(long lRow1) {
        ourDatabase.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + lRow1, null);
    }

    public int getCount(){
        String countQuery = "SELECT * FROM " + DATABASE_TABLE;
        Cursor c = ourDatabase.rawQuery(countQuery, null);
        int cnt = c.getCount();
        c.close();
        return cnt;
    }

    public int getStartRowID(){
        String[] columns = new String[] {KEY_ROW_ID, KEY_ID, KEY_NAME, KEY_MAC_ADDRESS, KEY_LAT,KEY_LNG};
        Cursor c = ourDatabase.query(DATABASE_TABLE,columns,null,null,null,null,null);
        String result = "";

        int iRow = c.getColumnIndex(KEY_ROW_ID);
        c.moveToFirst();
        String temp = c.getString(iRow);

        int tmp = Integer.parseInt(temp);


        return tmp;

    }

    public int getEndRowID(){
        String[] columns = new String[] {KEY_ROW_ID, KEY_ID, KEY_NAME, KEY_MAC_ADDRESS, KEY_LAT,KEY_LNG};
        Cursor c = ourDatabase.query(DATABASE_TABLE,columns,null,null,null,null,null);
        String result = "";

        int iRow = c.getColumnIndex(KEY_ROW_ID);
        c.moveToLast();
        String temp = c.getString(iRow);

        int tmp = Integer.parseInt(temp);


        return tmp;

    }




    private  static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" +
                            KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_ID + " TEXT NOT NULL, " +
                            KEY_NAME + " TEXT NOT NULL, " +
                            KEY_MAC_ADDRESS + " TEXT NOT NULL, " +
                            KEY_LAT + " TEXT NOT NULL, " +
                            KEY_LNG + " TEXT NOT NULL);"
            );

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    public DatabaseClass(Context c){
        ourContext = c;
    }

    public DatabaseClass open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        ourHelper.close();
    }

    public long createEntry(String id, String name, String macAddress, String Lat, String Lng) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_ID, id);
        cv.put(KEY_NAME, name);
        cv.put(KEY_MAC_ADDRESS, macAddress);
        cv.put(KEY_LAT, Lat);
        cv.put(KEY_LNG, Lng);
        return ourDatabase.insert(DATABASE_TABLE,null,cv);

    }

}

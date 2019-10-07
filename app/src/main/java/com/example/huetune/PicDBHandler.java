package com.example.huetune;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PicDBHandler extends SQLiteOpenHelper {

    /*private class myPic{
        String pic;
        String loc;
        String song;

        public myPic(String pic, String loc, String song){
            this.pic = pic;
            this.loc = loc;
            this.song = song;
        }

        String getPic(){
            return this.pic;
        }

        String getLoc(){
            return this.loc;
        }

        String getSong(){
            return this.song;
        }
    }
*/

    //Database version.
    //Note: Increase the database version every-time you make changes to your table structure.
    private static final int DATABASE_VERSION = 4;

    //Database Name
    private static final String DATABASE_NAME = "huepics";

    //You will declare all your table names here.
    private static final String TABLE_PICS = "pics";

    // pics Table Columns names
    private static final String KEY_PICS_ID = "picuri";
    private static final String KEY_PICS_GPS = "location";
    private static final String KEY_PICS_SONG = "song";
    private static final String KEY_PICS_SLINK = "slink";
    private static final String KEY_PICS_DATE = "date";

    //last deleted pic and pics
    private String lastdeletedpic = null;
    private ArrayList<String> lastdeletedpics = new ArrayList<>();
    private Cursor tmpcursor = null;


    //Here context passed will be of application and not activity.
    public PicDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //This method will be called every-time the file is called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Query to create table
        String CREATE_PICS_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_PICS + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_PICS_ID + " TEXT NOT NULL UNIQUE, "
                + KEY_PICS_GPS + " TEXT, "
                + KEY_PICS_SLINK + " TEXT, "
                + KEY_PICS_DATE + " TEXT, "
                + KEY_PICS_SONG + " TEXT " + ")";

        //Create table query executed in sqlite
        db.execSQL(CREATE_PICS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //This method will be called only if there is change in DATABASE_VERSION.

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PICS);

        // Create tables again
        onCreate(db);
    }

    // Add New Student
    public int addPic(String uri, String location, String song, String slink) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Content values use KEY-VALUE pair concept
        ContentValues values = new ContentValues();
        values.put(KEY_PICS_ID, uri);
        values.put(KEY_PICS_GPS, location);
        values.put(KEY_PICS_SLINK, slink);
        values.put(KEY_PICS_SONG, song);

        return (int)db.insert(TABLE_PICS, null, values);
    }

    /*public myPic getmyPic(int studentID) {

        SQLiteDatabase db = this.getReadableDatabase();


        //You can browse to the query method to know more about the arguments.
        Cursor cursor = db.query(TABLE_PICS,
                new String[] { KEY_PICS_ID, KEY_PICS_GPS, KEY_PICS_SONG },
                KEY_PICS_ID + "=?",
                new String[] { String.valueOf(studentID) },
                null,
                null,
                null,
                null);

        if (cursor != null)
            cursor.moveToFirst();

        myPic p = new myPic(cursor.getString(0), cursor.getString(1), cursor.getString(2));

        //Return Student
        return p;
    }

    // Getting All Students
    public List<myPic> getAllPic() {
        List<myPic> picList = new ArrayList<myPic>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PICS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                myPic student = new myPic(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2));
                picList.add(student);
            } while (cursor.moveToNext());
        }

        // return student list
        return picList;
    }*/

    public void updatePic(String pic, String newloc) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_GPS + " = " + "\"" + newloc + "\"" + " WHERE " + KEY_PICS_ID + " = " + "\"" + pic + "\"");
        db.close();
    }

    public void deletePic(String pic) {
        lastdeletedpic = pic;
        SQLiteDatabase db = this.getWritableDatabase();
        String timeStamp = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).format(new Date());
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + timeStamp + " WHERE " + KEY_PICS_ID + " = " + "\"" + lastdeletedpic + "\"");
        db.close();
    }

    public void resumePic(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + "NULL" + " WHERE " + KEY_PICS_ID + " = " + "\"" + lastdeletedpic + "\"");
        db.close();
    }

    public void resumePicFromBin(String key){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + "NULL" + " WHERE " + KEY_PICS_ID + " = " + "\"" + key + "\"");
        db.close();
    }

    public void deleteAllPics() {
        SQLiteDatabase db = this.getWritableDatabase();
        lastdeletedpics.clear();
        tmpcursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
        while(tmpcursor.moveToNext()) {
            lastdeletedpics.add(tmpcursor.getString(tmpcursor.getColumnIndexOrThrow("picuri")));
        }
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd", Locale.ITALY).format(new Date());
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + timeStamp);
        db.close();
        tmpcursor.close();
    }

    public void resumeAllPics() {
        SQLiteDatabase db = this.getWritableDatabase();
        for(int i = 0; i < lastdeletedpics.size(); i++) {
            db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + "NULL" + " WHERE " + KEY_PICS_ID + " = " + "\"" + lastdeletedpics.get(i) + "\"");
        }
        db.close();

    }

    public void resumeAllPicsFromBin() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + "NULL" + " WHERE " + KEY_PICS_DATE + " IS NOT NULL");
        db.close();

    }

    public void deleteOLDPics(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PICS + " WHERE " + KEY_PICS_DATE + " >= date('now','-30 day')");
        //salvando la data come anno/mese/giorno è possibile eseguire una comparazione tra stringhe
        //dato che SQLLite non accetta DATE coem tipo ma solo TEXT
        db.close();
    }
}

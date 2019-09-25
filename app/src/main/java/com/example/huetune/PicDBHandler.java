package com.example.huetune;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class PicDBHandler extends SQLiteOpenHelper {

    private class myPic{
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

    //Database version.
    //Note: Increase the database version every-time you make changes to your table structure.
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "huepics";

    //You will declare all your table names here.
    private static final String TABLE_PICS = "pics";

    // pics Table Columns names
    private static final String KEY_PICS_ID = "picuri";
    private static final String KEY_PICS_GPS = "location";
    private static final String KEY_PICS_SONG = "song";
    private static final String KEY_PICS_SLINK = "slink";


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
                + KEY_PICS_ID + " TEXT, "
                + KEY_PICS_GPS + " TEXT, "
                + KEY_PICS_SLINK + " TEXT, "
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

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Add New Student
    public void addPic(String uri, String location, String song, String slink) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Content values use KEY-VALUE pair concept
        ContentValues values = new ContentValues();
        values.put(KEY_PICS_ID, uri);
        values.put(KEY_PICS_GPS, location);
        values.put(KEY_PICS_SLINK, slink);
        values.put(KEY_PICS_SONG, song);

        db.insert(TABLE_PICS, null, values);
        db.close();
    }

    // Getting single student details through ID
    public myPic getmyPic(int studentID) {

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
    }

    public int updatePic(String pic, String newloc) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PICS_ID, pic);

        // updating student row
        return db.update(TABLE_PICS,
                values,
                KEY_PICS_GPS + " = ?",
                new String[] { String.valueOf(newloc)});

    }

    public void deletePic(String pic) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PICS, KEY_PICS_ID + " = ?",
                new String[] { String.valueOf(pic) });
        db.close();
    }

    public void deleteAllPics() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PICS);
        db.close();
    }
}

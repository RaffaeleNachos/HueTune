package com.example.huetune;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PicDBHandler extends SQLiteOpenHelper {

    //Versione Database, va incrementata ogni volta che si esegue un cambiamento alla struttura del database
    private static final int DATABASE_VERSION = 8;

    private static final String DATABASE_NAME = "huepics";

    //nome della tabella principale
    private static final String TABLE_PICS = "pics";

    //attributi
    private static final String KEY_PICS_ID = "pic";
    private static final String KEY_PICS_GPS = "location";
    private static final String KEY_PICS_SONG = "song";
    private static final String KEY_PICS_SLINK = "slink";
    private static final String KEY_PICS_DATE = "date";
    private static final String KEY_PICS_ROT = "rotation";

    //variabili per rendere possibile la UNDO della delete
    private String lastdeletedpic = null;
    private ArrayList<String> lastdeletedpics = new ArrayList<>();
    private Cursor tmpcursor = null;

    public PicDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

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
                + KEY_PICS_SONG + " TEXT, "
                + KEY_PICS_ROT + " TEXT " + ")";

        //Create table query executed in sqlite
        db.execSQL(CREATE_PICS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //This method will be called only if DATABASE_VERSION is changed

        //Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PICS);

        //Create new table
        onCreate(db);
    }

    //metodo per aggiugnere immagini scattate dalla fotocamera, ho possibile rotazione e ho un path
    public int addPic(String path, String location, String song, String slink, String rot) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PICS_ID, path);
        values.put(KEY_PICS_GPS, location);
        values.put(KEY_PICS_SLINK, slink);
        values.put(KEY_PICS_SONG, song);
        values.put(KEY_PICS_ROT, rot);

        return (int)db.insert(TABLE_PICS, null, values);
    }

    //metodo per aggiungere immagini dalla galleria, non ho rotazione e ho una uri
    public int addPic(String uri, String location, String song, String slink) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PICS_ID, uri);
        values.put(KEY_PICS_GPS, location);
        values.put(KEY_PICS_SLINK, slink);
        values.put(KEY_PICS_SONG, song);
        values.put(KEY_PICS_ROT, "0");

        return (int)db.insert(TABLE_PICS, null, values);
    }

    //metodo per aggiornare la posizione della foto
    public void updateLocPic(String pic, String newloc) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_GPS + " = " + "\"" + newloc + "\"" + " WHERE " + KEY_PICS_ID + " = " + "\"" + pic + "\"");
        db.close();
    }

    //metodo per aggiungere la canzone al ritorno dalla chiamata API Spotify
    public void updateSongPic(String pic, String newSong, String newSongLink) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_SONG + " = " + "\"" + newSong + "\"" + " WHERE " + KEY_PICS_ID + " = " + "\"" + pic + "\"");
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_SLINK + " = " + "\"" + newSongLink + "\"" + " WHERE " + KEY_PICS_ID + " = " + "\"" + pic + "\"");
        db.close();
    }

    //metodo per cancellare la singola foto (inserisco una data per poter cancellare le foto più vecchie di 30 giorni)
    public void deletePic(String pic) {
        lastdeletedpic = pic;
        SQLiteDatabase db = this.getWritableDatabase();
        String timeStamp = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).format(new Date());
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + timeStamp + " WHERE " + KEY_PICS_ID + " = " + "\"" + lastdeletedpic + "\"");
        db.close();
    }

    //metodo per UNDO singola pic
    public void resumePic(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + "NULL" + " WHERE " + KEY_PICS_ID + " = " + "\"" + lastdeletedpic + "\"");
        db.close();
    }

    //metodo per cancellare tutte le foto
    public void deleteAllPics() {
        SQLiteDatabase db = this.getWritableDatabase();
        lastdeletedpics.clear();
        tmpcursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
        while(tmpcursor.moveToNext()) {
            lastdeletedpics.add(tmpcursor.getString(tmpcursor.getColumnIndexOrThrow("pic")));
        }
        String timeStamp = new SimpleDateFormat("yyyy/MM/dd", Locale.ITALY).format(new Date());
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + timeStamp);
        db.close();
        tmpcursor.close();
    }

    //metodo per eseguire l'UNDO della cancellazione di tutte le foto
    public void resumeAllPics() {
        SQLiteDatabase db = this.getWritableDatabase();
        for(int i = 0; i < lastdeletedpics.size(); i++) {
            db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + "NULL" + " WHERE " + KEY_PICS_ID + " = " + "\"" + lastdeletedpics.get(i) + "\"");
        }
        db.close();

    }

    //metodo per ripristinare una foto dal cestino (HueBin) alla MainActivity
    public void resumePicFromBin(String key){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + "NULL" + " WHERE " + KEY_PICS_ID + " = " + "\"" + key + "\"");
        db.close();
    }

    //metodo per ripristinare tutte le foto dal cestino (HueBin) alla MainActivity
    public void resumeAllPicsFromBin() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_PICS + " SET " + KEY_PICS_DATE + " = " + "NULL" + " WHERE " + KEY_PICS_DATE + " IS NOT NULL");
        db.close();

    }

    //metodo che mi cancella tutte le foto nel cestino più vecchie di 30 giorni
    public void deleteOLDPics(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PICS + " WHERE " + KEY_PICS_DATE + " >= date('now','-30 day')");
        //salvando la data come anno/mese/giorno è possibile eseguire una comparazione tra stringhe
        //dato che SQLLite non accetta DATE come tipo ma solo TEXT
        db.close();
    }
}

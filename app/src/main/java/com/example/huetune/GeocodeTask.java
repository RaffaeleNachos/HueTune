package com.example.huetune;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class GeocodeTask extends AsyncTask<String, Void, Void> {

    //uso le weak reference perchè il Garbage Collector è mio amico!
    private final WeakReference<PicDBHandler> handler;
    private final WeakReference<MyAdapter> adapter;
    private final WeakReference<Geocoder> geocoder;
    private final WeakReference<String> mypath;
    private List<Address> addresses = null;

    GeocodeTask(PicDBHandler handler, MyAdapter adapter, Geocoder geocoder, String mypath){
        this.handler = new WeakReference<>(handler);
        this.adapter = new WeakReference<>(adapter);
        this.geocoder = new WeakReference<>(geocoder);
        this.mypath = new WeakReference<>(mypath);
    }

    @Override
    protected Void doInBackground(String... inputfile) {
        Geocoder geo = geocoder.get();
        if(inputfile[0]!=null) {
            ExifInterface myexif = null;
            try {
                myexif = new ExifInterface(inputfile[0]); //ho i miei metadati
            } catch (IOException e) {
                e.printStackTrace();
            }
            double[] latlong = null;
            if (myexif != null) {
                latlong = myexif.getLatLong(); //mi faccio restituire lat e long in un array di double
            }
            if(latlong!=null) {
                try {
                    addresses = geo.getFromLocation(latlong[0], latlong[1], 1); //chiamata al geocoder restituisce una lista di Address
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void voids) { //eseguito nel Thread UI
        PicDBHandler tmpHandler = handler.get();
        MyAdapter tmpAdapter = adapter.get();
        String mykey = mypath.get();
        if (addresses!=null) {
            tmpHandler.updateLocPic(mykey, addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea()); //prendo il primo della lista che dovrebbe essere il più accurato
        } else {
            tmpHandler.updateLocPic(mykey, "Choose Location");
        }
        SQLiteDatabase db = tmpHandler.getWritableDatabase();
        Cursor myCursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
        tmpAdapter.changeCursor(myCursor);
    }
}

package com.example.huetune;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;


import androidx.exifinterface.media.ExifInterface;


import java.io.IOException;
import java.util.List;

public class GeocodeTask extends AsyncTask<String, Void, Void> {

    private List<Address> addresses;
    private PicDBHandler handler;
    private MyAdapter adapter;
    private Geocoder geocoder;
    private String mykey;

    GeocodeTask(PicDBHandler handler, MyAdapter adapter, Geocoder geocoder, String myuri){
        this.handler=handler;
        this.adapter=adapter;
        this.geocoder=geocoder;
        this.mykey=myuri;
    }

    @Override
    protected Void doInBackground(String... inputfile) {
        if(inputfile[0]!=null) {
            ExifInterface myexif = null;
            try {
                myexif = new ExifInterface(inputfile[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            double[] latlong = null;
            if (myexif != null) {
                latlong = myexif.getLatLong();
            }
            if(latlong!=null) {
                try {
                    addresses = geocoder.getFromLocation(latlong[0], latlong[1], 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void voids) { //eseguito nel Thread UI
        handler.updateLocPic(mykey,addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea());
        SQLiteDatabase db = handler.getWritableDatabase();
        Cursor myCursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
        adapter.changeCursor(myCursor);
    }
}

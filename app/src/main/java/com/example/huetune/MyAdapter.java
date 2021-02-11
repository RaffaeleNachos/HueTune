package com.example.huetune;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;  //Glide è una alternativa al codice che ho scritto per caricare in Async

public class MyAdapter extends CursorAdapter {

    public MyAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.view_huepic, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView myImg = view.findViewById(R.id.photoView);
        TextView myLoc = view.findViewById(R.id.locationView);
        myLoc.setSelected(true);
        TextView mySong = view.findViewById(R.id.songView);
        mySong.setSelected(true);
        String img = cursor.getString(cursor.getColumnIndexOrThrow("pic"));
        String loc = cursor.getString(cursor.getColumnIndexOrThrow("location"));
        String tune = cursor.getString(cursor.getColumnIndexOrThrow("song"));
        String rot = cursor.getString(cursor.getColumnIndexOrThrow("rotation"));
        //setto un placeholder
        //myImg.setImageDrawable(context.getResources().getDrawable(R.drawable.placeholder));
        //myImg.setImageURI(Uri.parse(img)); //SUPER SLOW! Eseguo in Async
        //new ImageLoaderClass(myImg, context).execute(img,rot);
        //Glide è una libreria ottima (funzionano anche le gif animate!)
        //sto usando un asynctask solo per scopi universitari, per poter far vedere come eseguire un caricamente asincrono.
        Glide.with(view).load(img).centerCrop().into(myImg); //library that uses asyncTask and caching, fast and super simple
        myLoc.setText(String.valueOf(loc));
        mySong.setText(String.valueOf(tune));
    }
}
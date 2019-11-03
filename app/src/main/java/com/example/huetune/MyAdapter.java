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
        myImg.setVisibility(View.INVISIBLE);
        TextView myLoc = view.findViewById(R.id.locationView);
        myLoc.setSelected(true);
        TextView mySong = view.findViewById(R.id.songView);
        mySong.setSelected(true);
        String img = cursor.getString(cursor.getColumnIndexOrThrow("picuri"));
        String loc = cursor.getString(cursor.getColumnIndexOrThrow("location"));
        String tune = cursor.getString(cursor.getColumnIndexOrThrow("song"));
        //myImg.setImageURI(Uri.parse(img)); //SUPER SLOW! Should do it with async
        new ImageLoaderClass(myImg, context).execute(img);
        //Glide.with(view).load(img).centerCrop().into(myImg); //library that uses asyncTask and caching, fast and super simple
        myLoc.setText(String.valueOf(loc));
        mySong.setText(String.valueOf(tune));
    }
}
package com.example.huetune;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyAdapter extends CursorAdapter {

    public MyAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.view_huepic, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        ImageView myImg = view.findViewById(R.id.photoView);
        TextView myLoc = view.findViewById(R.id.locationView);
        myLoc.setSelected(true);
        TextView mySong = view.findViewById(R.id.songView);
        mySong.setSelected(true);
        // Extract properties from cursor
        String img = cursor.getString(cursor.getColumnIndexOrThrow("picuri"));
        String loc = cursor.getString(cursor.getColumnIndexOrThrow("location"));
        String tune = cursor.getString(cursor.getColumnIndexOrThrow("song"));
        // Populate fields with extracted properties
        myImg.setImageURI(Uri.parse(img));
        myLoc.setText(String.valueOf(loc));
        mySong.setText(String.valueOf(tune));
    }
}


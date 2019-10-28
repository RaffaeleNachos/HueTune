package com.example.huetune;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ImageLoaderClass extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<Context> ctx;

    ImageLoaderClass(ImageView imageView, Context ctx) {
        imageViewReference = new WeakReference<>(imageView);
        this.ctx = new WeakReference<>(ctx);
    }

    @Override
    protected Bitmap doInBackground(String... str) {
        InputStream image_stream = null;
        try {
            Context myctx = ctx.get();
            image_stream = myctx.getContentResolver().openInputStream(Uri.parse(str[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap myBitmap = BitmapFactory.decodeStream(image_stream);
        try {
            if (image_stream != null) {
                image_stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        myBitmap = Bitmap.createScaledBitmap(myBitmap, myBitmap.getWidth()/5, myBitmap.getHeight()/5, false);
        return myBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) { //eseguito nel Thread UI
        if (isCancelled()) {
            bitmap = null;
        }

        ImageView imageView = imageViewReference.get();
        if (imageView != null) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            } else {
                Log.w("errore", "impossibile caricare immagine");
            }
        }
    }
}

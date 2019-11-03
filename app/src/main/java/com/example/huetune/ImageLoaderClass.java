package com.example.huetune;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ImageLoaderClass extends AsyncTask<String, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<Context> ctx;
    private InputStream image_stream;

    ImageLoaderClass(ImageView imageView, Context ctx) {
        imageViewReference = new WeakReference<>(imageView);
        this.ctx = new WeakReference<>(ctx);
        image_stream = null;
    }

    @Override
    protected Bitmap doInBackground(String... str) {
        image_stream = null;
        try {
            Context myctx = ctx.get();
            image_stream = myctx.getContentResolver().openInputStream(Uri.parse(str[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            image_stream = null;
        }
        Bitmap myBitmap = null;
        try {
            if (image_stream != null) {
                myBitmap = BitmapFactory.decodeStream(image_stream);
                image_stream.close();
                myBitmap = Bitmap.createScaledBitmap(myBitmap, myBitmap.getWidth()/5, myBitmap.getHeight()/5, false);
                //modo più veloce per fixare la rotazione dell'immagine nei telefoni che scattano in landscape mode anche se messi in portrait
                //la soluzione più efficiente e corretta sarebbe andare a leggere gli exif che contengono una etichetta ExifInterface.TAG_ORIENTATION e ruotare rispetto quel valore
                if (str[0].contains("HUETUNE")) {
                    myBitmap = rotateImage(myBitmap, 90);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) { //eseguito nel Thread UI
        if (isCancelled()) { //se il task viene killato
            bitmap = null;
        }

        ImageView imageView = imageViewReference.get();
        if (imageView != null) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            } else {
                Context myctx = ctx.get();
                Toast.makeText(myctx, "Error Loading Image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) { //metodo di utilità per ruotare bitmap
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}

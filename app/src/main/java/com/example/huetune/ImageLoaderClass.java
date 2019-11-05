package com.example.huetune;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ImageLoaderClass extends AsyncTask<String, Void, Bitmap> {

    //uso le weak reference perchè il Garbage Collector è mio amico!
    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<Context> ctx;

    ImageLoaderClass(ImageView imageView, Context ctx) {
        this.imageViewReference = new WeakReference<>(imageView);
        this.ctx = new WeakReference<>(ctx);
    }

    @Override
    protected Bitmap doInBackground(String... path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //visto che non  ho bisogno di una immagine in alta qualità (ma una sorta di thumbnail) mi basta caricare 1 pixel ogni 16
        options.inSampleSize = 16;

        Bitmap myBitmap;
        if (path[0].contains("content://")) { //se è una URI
            Context tmpCtx = ctx.get();
            InputStream image_stream = null;
            try {
                image_stream = tmpCtx.getContentResolver().openInputStream(Uri.parse(path[0]));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            myBitmap = BitmapFactory.decodeStream(image_stream);
        }
        else { //se è un path
            myBitmap = BitmapFactory.decodeFile(path[0]);
        }
        switch(Integer.parseInt(path[1])) { //a questo punto controllo se l'immagine è ruotata
            case 6:
                myBitmap = rotateImage(myBitmap, 90);
                break;

            case 4:
                myBitmap = rotateImage(myBitmap, 180);
                break;

            case 8:
                myBitmap = rotateImage(myBitmap, 270);
                break;
        }
        //ottengo una bitmap croppata al centro ed inoltre mybitmap viene riciclata in modo tale da non occupare troppa memoria!
        myBitmap = ThumbnailUtils.extractThumbnail(myBitmap, 300, 300, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        //myBitmap = Bitmap.createScaledBitmap(myBitmap, myBitmap.getWidth()/5, myBitmap.getHeight()/5, false); //abbastanza lento e spreco memoria
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
            } else {
                Context myctx = ctx.get();
                Toast.makeText(myctx, "Error Loading Image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //metodo di utilità per ruotare bitmap
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}

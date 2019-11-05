package com.example.huetune;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetSpotifySongWithAI extends AsyncTask<String, Void, Void> {

    //uso le weak reference perchè il Garbage Collector è mio amico!
    private final WeakReference<Context> myctx;
    private final WeakReference<PicDBHandler> handler;
    private final WeakReference<MyAdapter> adapter;
    private final WeakReference<String> sessionToken;
    private final WeakReference<Activity> myActivity;
    private RequestQueue requestQueue;

    GetSpotifySongWithAI(Context ctx, PicDBHandler handler, MyAdapter adapter, String sessionToken, Activity myActivity){
        this.myctx = new WeakReference<>(ctx);
        this.handler = new WeakReference<>(handler);
        this.adapter = new WeakReference<>(adapter);
        this.sessionToken = new WeakReference<>(sessionToken);
        this.myActivity = new WeakReference<>(myActivity);
        requestQueue = Volley.newRequestQueue(ctx);
    }

    @Override
    protected Void doInBackground(String... str) {
        //se è una URI
        if (str[0].contains("content://")) mySpotifyGET(Uri.parse(str[0]));
        else mySpotifyGET(str[0]); //se è un path
        return null;
    }

    //chiamata API a Spotify con PATH!
    private void mySpotifyGET(final String tmpPath){
        final Context tmpCtx = myctx.get();
        final PicDBHandler tmpHandler = handler.get();
        final MyAdapter tmpAdapter = adapter.get();
        final String tmpToken = sessionToken.get();
        Activity tmpActivity = myActivity.get();

        //TensorFlowLITE in azione
        List<Classifier.Recognition> output = null;
        try {
            Bitmap myBitmap = BitmapFactory.decodeFile(tmpPath);
            myBitmap = Bitmap.createScaledBitmap(myBitmap, 224, 224, false);
            //classificatore quantizzato, occupa molta meno memoria di uno basato su punti float32
            ClassifierQuantizedMobileNet myImgClass = new ClassifierQuantizedMobileNet(tmpActivity, 1);
            output = myImgClass.recognizeImage(myBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String requrl = "https://api.spotify.com/v1/search?q=" + output.get(0).getTitle().replaceAll("\\s", "%20") + "&type=track&market=IT&limit=1&offset=0";
        JsonObjectRequest jsonreq = new JsonObjectRequest
                (Request.Method.GET, requrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject tracks;
                        JSONArray items;
                        JSONObject urls;
                        String songlink = null;
                        String songname = null;
                        JSONArray artists;
                        JSONObject artist;
                        String artistname = null;
                        try {
                            tracks = response.getJSONObject("tracks");
                            items = tracks.getJSONArray("items");
                            urls = (JSONObject) items.get(0);
                            songname = urls.getString("name");
                            songlink = urls.getJSONObject("external_urls").getString("spotify");
                            artists = urls.getJSONArray("artists");
                            artist = (JSONObject) artists.get(0);
                            artistname = artist.getString("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Log.w("songname", songname);
                        //Log.w("artistname", artistname);
                        //Log.w("songlink", songlink);
                        //Log.w("jsonresp", response.toString().replaceAll("\\\\", ""));
                        //ADD TO DB
                        tmpHandler.updateSongPic(tmpPath, songname + " - " + artistname, songlink);
                        SQLiteDatabase db = tmpHandler.getWritableDatabase();
                        Cursor myCursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
                        tmpAdapter.changeCursor(myCursor);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //ADD TO DB WITH ERROR MAY BE A BETTER OPTION?
                        Toast.makeText(tmpCtx, "Spotify Response Error", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders(){
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + tmpToken);
                return headers;
            }
        };
        requestQueue.add(jsonreq);
    }

    //chiamata a API Spotify avendo una URI
    private void mySpotifyGET(final Uri tmpUri){
        final Context tmpCtx = myctx.get();
        final PicDBHandler tmpHandler = handler.get();
        final MyAdapter tmpAdapter = adapter.get();
        final String tmpToken = sessionToken.get();
        Activity tmpActivity = myActivity.get();

        //TensorFlowLITE in azione
        List<Classifier.Recognition> output = null;
        try {
            InputStream image_stream = tmpCtx.getContentResolver().openInputStream(tmpUri);
            Bitmap myBitmap = BitmapFactory.decodeStream(image_stream);
            myBitmap = Bitmap.createScaledBitmap(myBitmap, 224, 224, false);
            ClassifierQuantizedMobileNet myImgClass = new ClassifierQuantizedMobileNet(tmpActivity, 1);
            output = myImgClass.recognizeImage(myBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String requrl = "https://api.spotify.com/v1/search?q=" + output.get(0).getTitle().replaceAll("\\s", "%20") + "&type=track&market=IT&limit=1&offset=0";
        JsonObjectRequest jsonreq = new JsonObjectRequest
                (Request.Method.GET, requrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject tracks;
                        JSONArray items;
                        JSONObject urls;
                        String songlink = null;
                        String songname = null;
                        JSONArray artists;
                        JSONObject artist;
                        String artistname = null;
                        try {
                            tracks = response.getJSONObject("tracks");
                            items = tracks.getJSONArray("items");
                            urls = (JSONObject) items.get(0);
                            songname = urls.getString("name");
                            songlink = urls.getJSONObject("external_urls").getString("spotify");
                            artists = urls.getJSONArray("artists");
                            artist = (JSONObject) artists.get(0);
                            artistname = artist.getString("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Log.w("songname", songname);
                        //Log.w("artistname", artistname);
                        //Log.w("songlink", songlink);
                        //Log.w("jsonresp", response.toString().replaceAll("\\\\", ""));
                        //ADD TO DB
                        tmpHandler.updateSongPic(tmpUri.toString(), songname + " - " + artistname, songlink);
                        SQLiteDatabase db = tmpHandler.getWritableDatabase();
                        Cursor myCursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
                        tmpAdapter.changeCursor(myCursor);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(tmpCtx, "Spotify Response Error", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders(){
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + tmpToken);
                return headers;
            }
        };
        requestQueue.add(jsonreq);
    }
}

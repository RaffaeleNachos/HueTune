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
    //TODO weakreference
    private Context ctx;
    private RequestQueue requestQueue;
    private PicDBHandler handler;
    private MyAdapter adapter;
    private String sessionToken;
    private Activity myActivity;

    GetSpotifySongWithAI(Context ctx, PicDBHandler handler, MyAdapter adapter, String sessionToken, Activity myActivity){
        this.ctx = ctx;
        this.handler = handler;
        this.adapter = adapter;
        this.sessionToken = sessionToken;
        this.myActivity=myActivity;
        requestQueue = Volley.newRequestQueue(ctx);
    }

    @Override
    protected Void doInBackground(String... str) {
        mySpotifyGET(str[0]);
        return null;
    }

    //search call web api spotify
    private void mySpotifyGET(final String tmpUri){
        //TFLITE TRY
        List<Classifier.Recognition> output = null;
        try {
            InputStream image_stream = ctx.getContentResolver().openInputStream(Uri.parse(tmpUri));
            Bitmap myBitmap = BitmapFactory.decodeStream(image_stream);
            myBitmap = Bitmap.createScaledBitmap(myBitmap, 224, 224, false);
            ClassifierQuantizedMobileNet myImgClass = new ClassifierQuantizedMobileNet(myActivity, 1);
            output = myImgClass.recognizeImage(myBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String requrl = "https://api.spotify.com/v1/search?q=" + output.get(0).getTitle() + "&type=track&market=IT&limit=1&offset=0";
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
                        handler.updateSongPic(tmpUri, songname + " - " + artistname, songlink);
                        SQLiteDatabase db = handler.getWritableDatabase();
                        Cursor myCursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
                        adapter.changeCursor(myCursor);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.w("errorresp", error.toString());
                        //ADD TO DB WITH ERROR
                        Toast.makeText(ctx, "Spotify Response Error", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders(){
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + sessionToken);
                return headers;
            }
        };
        requestQueue.add(jsonreq);
    }
}

package com.example.huetune;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;

import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private Boolean clickadd = false;
    private FloatingActionButton btncam, btngal, btnadd;
    private LinearLayout llc, llg;
    private int PICK_IMAGE_REQUEST;
    private int TAKE_IMAGE_REQUEST;
    private int AUTOCOMPLETE_REQUEST_CODE;
    private PicDBHandler handler;
    private MyAdapter adapter;
    private Cursor myCursor;
    private SQLiteDatabase db;
    private String currentPhotoPath;
    private SearchView searchvw;
    private Geocoder geocoder;
    private List<Address> addresses;
    private RequestQueue requestQueue;
    private String requrl = "https://api.spotify.com/v1/search?";
    private String sessionToken = null;
    private ListView lview;
    private Cursor tmpcursor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FAB

        btncam = findViewById(R.id.addc);
        llc = findViewById(R.id.llc);
        btncam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llc.setVisibility(View.INVISIBLE);
                llg.setVisibility(View.INVISIBLE);
                llc.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_close));
                llg.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_close));
                btncam.setClickable(false);
                btngal.setClickable(false);
                clickadd = false;
                reqImageC();
            }
        });


        btngal = findViewById(R.id.addg);
        llg = findViewById(R.id.llg);
        btngal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llc.setVisibility(View.INVISIBLE);
                llg.setVisibility(View.INVISIBLE);
                llc.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_close));
                llg.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_close));
                btncam.setClickable(false);
                btngal.setClickable(false);
                clickadd = false;
                reqImageG();
            }
        });

        btnadd = findViewById(R.id.add);
        btnadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickadd) {
                    llc.setVisibility(View.INVISIBLE);
                    llg.setVisibility(View.INVISIBLE);
                    llc.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_close));
                    llg.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_close));
                    btncam.setClickable(false);
                    btngal.setClickable(false);
                    clickadd = false;
                }
                else {
                    llc.setVisibility(View.VISIBLE);
                    llg.setVisibility(View.VISIBLE);
                    llc.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_open));
                    llg.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_open));
                    btncam.setClickable(true);
                    btngal.setClickable(true);
                    clickadd = true;
                }
            }
        });


        //DATABASE
        handler = new PicDBHandler(this);

        db = handler.getWritableDatabase();
        myCursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);

        lview = findViewById(R.id.listview);
        adapter = new MyAdapter(this, myCursor);
        lview.setAdapter(adapter);

        //attivo menu contestuale floating
        registerForContextMenu(lview);

        //FILTER QUERY FOR SEARCH
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String query = "SELECT _id,* FROM pics WHERE date IS NULL and location like '%" + constraint + "%' ";
                return db.rawQuery(query, null);
            }
        });

        //POSITION
        geocoder = new Geocoder(this, Locale.getDefault());
        //testare se geocoder è presente nel paese DA FARE

        //API FOR PLACE AUTOCOMPLETE
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCpyTKh4KmzLSWMV1jiD-S2FADGxtXLKC4");
        }

        //REST CALL TO SPOTIFY
        requestQueue = Volley.newRequestQueue(this);
        getSpotifyToken();
    }

    //metod to add search and oth opt threedots
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchvwi = menu.findItem(R.id.action_search);
        searchvw = (SearchView) searchvwi.getActionView();
        return true;
    }

    //menu per listview
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.cm_actions , menu);
    }

    //selection threedots menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_bin) {
            Toast.makeText(MainActivity.this, "Action settings clicked", Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.action_search) {
            searchvw.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(searchvw.getQuery().toString());
                    adapter.notifyDataSetChanged();
                    return true;
                }
            });
            return true;
        }
        if (id == R.id.action_del) {
            Snackbar sbar = Snackbar.make(findViewById(R.id.myFABLayout), "All Photos Deleted", Snackbar.LENGTH_LONG);
            sbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.resumeAllPics();
                    db = handler.getWritableDatabase();
                    Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
                    adapter.changeCursor(cursor);
                }
            });
            LinearLayout llc = findViewById(R.id.llc);
            LinearLayout llg = findViewById(R.id.llg);
            llc.setVisibility(View.INVISIBLE);
            llg.setVisibility(View.INVISIBLE);
            btncam.setClickable(false);
            btngal.setClickable(false);
            if(clickadd) {
                llc.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_close));
                llg.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_close));
            }
            clickadd=false;
            handler.deleteAllPics();
            db = handler.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
            adapter.changeCursor(cursor);
            sbar.show();
        }

        return super.onOptionsItemSelected(item);
    }

    //pressione lunga su item
    @Override
    public boolean onContextItemSelected(final MenuItem item) {

        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int itemPosition = info.position;
        tmpcursor = adapter.getCursor();
        tmpcursor.moveToPosition(itemPosition);

        switch(item.getItemId()){
            case R.id.cm_id_change:
                AUTOCOMPLETE_REQUEST_CODE = 3;
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.cm_id_play:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(tmpcursor.getString(tmpcursor.getColumnIndexOrThrow("slink"))));
                startActivity(browserIntent);
                break;
            case R.id.cm_id_btshare:
                Toast.makeText(this, "btshare"   , Toast.LENGTH_SHORT).show();
                break;
            case R.id.cm_id_delete:
                Toast.makeText(this, "delete " , Toast.LENGTH_SHORT).show();
                handler.deletePic(tmpcursor.getString(tmpcursor.getColumnIndexOrThrow("picuri")));
                db = handler.getWritableDatabase();
                Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
                adapter.changeCursor(cursor);
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TAKE FROM GALLERY ANCORA NON FUNZIONANTE ASPETTO INTEGRAZIONE AI
        if (requestCode == PICK_IMAGE_REQUEST && resultCode==Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            handler.addPic(imageUri.toString(), "Choose Position", "Song", "Songlink");
            db = handler.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
            adapter.changeCursor(cursor);
        }
        if (requestCode == TAKE_IMAGE_REQUEST && resultCode==Activity.RESULT_OK) {
            mySpotifyGET();
        }
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode==Activity.RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            handler.updatePic(tmpcursor.getString(tmpcursor.getColumnIndexOrThrow("picuri")), place.getName());
            db = handler.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
            adapter.changeCursor(cursor);
        }
    }

    private void reqImageG(){
        PICK_IMAGE_REQUEST = 1;
        Intent picki = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        picki.setType("image/*");
        startActivityForResult(picki, PICK_IMAGE_REQUEST);
    }
    private void reqImageC() {
        TAKE_IMAGE_REQUEST = 2;
        Intent takei = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File takenpic = null;
        try{
            takenpic=createImageFile();
        } catch (IOException e) {
            Log.println(Log.ERROR, "IOException", "Error creating picture file");
        }
        if(takenpic!=null){
            Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.provider", takenpic);
            takei.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takei, TAKE_IMAGE_REQUEST);
        }
    }

    //most from developer.android doc
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(new Date());
        String imageFileName = "HUETUNE_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private String getMyPosition(String inputfile) {
        ExifInterface myexif = null;
        try {
            myexif = new ExifInterface(inputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        float[] latlong = new float[2];
        boolean hasLatLong = myexif.getLatLong(latlong);
        if (hasLatLong) {
            try {
                addresses = geocoder.getFromLocation(latlong[0], latlong[1], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses.get(0).getLocality()+", "+addresses.get(0).getAdminArea();
        }
        return "Choose Position";
    }

    private void getSpotifyToken(){
        String rest = "https://accounts.spotify.com/api/token";
        StringRequest jsonreq = new StringRequest  //uso stringrequest perchè JSONObjreq fa override di getparams() e non chiama il metodo
                (Request.Method.POST, rest, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject token = null;
                        try {
                            token = new JSONObject(response);
                            sessionToken = token.getString("access_token");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.w("errorresp", error.toString());
                            }
                        })
        {
            // -H parametes
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                String mydashkey = "24ffb05d1e82431b91638ab90386fc84:710d0f96762c4d4ca6c81d97aec82556"; //trasformo codiceid:keyid di spotify in base 64
                mydashkey = Base64.encodeToString(mydashkey.getBytes(), Base64.NO_WRAP); //nowrap per evitare \n
                mydashkey = "Basic " + mydashkey;
                Log.w("key", mydashkey);
                headers.put("Authorization", mydashkey);
                return headers;
            }
            // -d parameters
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("grant_type", "client_credentials");
                return params;
            }
        };
        requestQueue.add(jsonreq);
    }

    private void mySpotifyGET(){
        final String mypos = getMyPosition(currentPhotoPath);
        requrl = requrl + "q=" + mypos + "&type=track&market=IT&limit=1&offset=0";
        JsonObjectRequest jsonreq = new JsonObjectRequest
                (Request.Method.GET, requrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject tracks = null;
                        JSONArray items = null;
                        JSONObject urls = null;
                        String songlink = null;
                        String songname = null;
                        JSONArray artists = null;
                        JSONObject artist = null;
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
                        Log.w("songname", songname);
                        Log.w("artistname", artistname);
                        Log.w("songlink", songlink);
                        Log.w("jsonresp", response.toString().replaceAll("\\\\", ""));
                        //ADD TO DB
                        Uri imageUri = Uri.fromFile(new File(currentPhotoPath));
                        //get della posizione da fare assolutamente in asynctask
                        handler.addPic(imageUri.toString(), mypos, songname + " - " + artistname, songlink);
                        db = handler.getWritableDatabase();
                        Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
                        adapter.changeCursor(cursor);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.w("errorresp", error.toString());
                    }
                })
        {
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + sessionToken);
                return headers;
            }
        };
        requestQueue.add(jsonreq);
    }
}

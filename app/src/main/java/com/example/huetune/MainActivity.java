package com.example.huetune;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
    private int PERM_REQUEST_INTERNET = 4;
    private int PERM_REQUEST_WREXT = 5;
    private int PERM_REQUEST_GPS = 6;
    public PicDBHandler handler;
    private MyAdapter adapter;
    private Cursor myCursor;
    private SQLiteDatabase db;
    private String currentPhotoPath;
    private SearchView searchvw;
    private Geocoder geocoder;
    private List<Address> addresses;
    private RequestQueue requestQueue;
    private String sessionToken = null;
    private ListView lview;
    private Cursor tmpcursor;
    private Integer itemPosition;
    private FusedLocationProviderClient fusedLocationClient;

    //TODO make update queries aynctask

    @Override
    protected void onResume(){ //per quando ritorna da HueBin
        super.onResume();
        db = handler.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
        adapter.changeCursor(cursor);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TOOLBAR
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
                //bug noto flipped shadow https://issuetracker.google.com/issues/132569416 possibile soluzione applicare animazione a singoli elementi textview e fab
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
                //PERMISSIONS FOR PHOTOS
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERM_REQUEST_WREXT);
                }
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERM_REQUEST_WREXT);
                }
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

        //PERMISSIONS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    PERM_REQUEST_INTERNET);
        }

        //DATABASE
        handler = new PicDBHandler(this);
        handler.deleteOLDPics(); //cancella le foto nel cestino più vecchie di 30 giorni

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

        //POSITION AND GPS WITH PLAY SERVICES BATTERY SAVEEEEEEE
        geocoder = new Geocoder(this, Locale.getDefault());
        if (geocoder.isPresent()==false){
            Toast.makeText(this, "Impossible to know location from taken photos", Toast.LENGTH_SHORT).show();
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        //REST CALL TO SPOTIFY
        requestQueue = Volley.newRequestQueue(this);
        getSpotifyToken();
    }

    //inflate menu in toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchvwi = menu.findItem(R.id.action_search);
        searchvw = (SearchView) searchvwi.getActionView();
        return true;
    }

    //inflate custom menu from listview item
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.cm_actions , menu);
    }

    //select toolbar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_bin) {
            Intent intent = new Intent(this, HueBinActivity.class);
            startActivity(intent);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //long press on item in listview
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        itemPosition = info.position;

        int id = item.getItemId();
        if(id == R.id.cm_id_curr) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERM_REQUEST_GPS);
            }
            tmpcursor = adapter.getCursor();
            tmpcursor.moveToPosition(itemPosition); //si sposta all'indice
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                handler.updatePic(tmpcursor.getString(tmpcursor.getColumnIndexOrThrow("picuri")),location.toString());
                                db = handler.getWritableDatabase();
                                Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
                                adapter.changeCursor(cursor);
                            }
                        }
                    });
            return true;
        }
        if(id == R.id.cm_id_change) {
            AUTOCOMPLETE_REQUEST_CODE = 3;
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken("pk.eyJ1IjoibmFjaG9zZnB2IiwiYSI6ImNrMHp2Z213NTA1M3ozY25xNG5vMzh0Nm4ifQ.RlsNT4-jUqoKP85bMOUZjg")
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#EEEEEE"))
                            .limit(10)
                            .build(PlaceOptions.MODE_CARDS))
                    .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            return true;
        }
        if(id == R.id.cm_id_play) {
            tmpcursor = adapter.getCursor();
            tmpcursor.moveToPosition(itemPosition);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(tmpcursor.getString(tmpcursor.getColumnIndexOrThrow("slink"))));
            startActivity(browserIntent);
            return true;
        }
        if(id == R.id.cm_id_btshare) {
            tmpcursor = adapter.getCursor();
            tmpcursor.moveToPosition(itemPosition);
            Toast.makeText(this, "btshare", Toast.LENGTH_SHORT).show();
            return true;
        }
        if(id == R.id.cm_id_delete) {
            tmpcursor = adapter.getCursor();
            tmpcursor.moveToPosition(itemPosition);
            Snackbar sbar = Snackbar.make(findViewById(R.id.myFABLayout), "Photo Deleted", Snackbar.LENGTH_LONG);
            sbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handler.resumePic();
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
            handler.deletePic(tmpcursor.getString(tmpcursor.getColumnIndexOrThrow("picuri")));
            db = handler.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
            adapter.changeCursor(cursor);
            sbar.show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    //on activity intent result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TAKE FROM GALLERY ANCORA NON FUNZIONANTE ASPETTO INTEGRAZIONE AI
        if (requestCode == PICK_IMAGE_REQUEST && resultCode==Activity.RESULT_OK) {
            Uri imageUri = data.getData(); //data in questa risposta è la uri e un flag sconosciuto boh
            if (imageUri != null && handler.addPic(imageUri.toString(), "Choose Position", "Song", "Songlink") == -1) {
                Toast.makeText(this, "Photo already present", Toast.LENGTH_SHORT).show();
            }
            db = handler.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
            adapter.changeCursor(cursor);
        }
        if (requestCode == TAKE_IMAGE_REQUEST && resultCode==Activity.RESULT_OK) {
            mySpotifyGET();
        }
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode==Activity.RESULT_OK) {
            tmpcursor = adapter.getCursor();
            tmpcursor.moveToPosition(itemPosition);
            CarmenFeature feature = PlaceAutocomplete.getPlace(data);
            handler.updatePic(tmpcursor.getString(tmpcursor.getColumnIndexOrThrow("picuri")), feature.placeName());
            db = handler.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
            adapter.changeCursor(cursor);
        }
    }

    //take image from gallery
    private void reqImageG(){
        PICK_IMAGE_REQUEST = 1;
        Intent picki = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        picki.setType("image/*");
        startActivityForResult(picki, PICK_IMAGE_REQUEST);
    }
    //take image from camera
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

    //most from developer.android doc create file image when captured on camera
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(new Date());
        String imageFileName = "HUETUNE_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);  //restituisce la directory sulla sd che quando disinstalli elimina tutto
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //position taken from exif data
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
            return addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea();
        }
        return "Choose Position";
    }

    //get session token from soptify web api call
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
                                //Log.w("errorresp", error.toString());
                                Toast.makeText(MainActivity.this, "Spotify Token not received, Restart App", Toast.LENGTH_SHORT).show();
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
                //Log.w("key", mydashkey);
                headers.put("Authorization", mydashkey);
                return headers;
            }
            // -d parameters
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("grant_type", "client_credentials");
                return params;
            }
        };
        requestQueue.add(jsonreq);
    }

    //search call web api spotify
    private void mySpotifyGET(){
        //TFLITE TRY
        List<Classifier.Recognition> output = null;
        try {
            Bitmap myBitmap = BitmapFactory.decodeFile(currentPhotoPath);
            myBitmap = Bitmap.createScaledBitmap(myBitmap, 224, 224, false);
            ClassifierQuantizedMobileNet myImgClass = new ClassifierQuantizedMobileNet(MainActivity.this, 1);
            output = myImgClass.recognizeImage(myBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String requrl = "https://api.spotify.com/v1/search?q=" + output.get(0).getTitle() + "&type=track&market=IT&limit=1&offset=0";
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
                        handler.addPic(imageUri.toString(), getMyPosition(currentPhotoPath), songname + " - " + artistname, songlink);
                        db = handler.getWritableDatabase();
                        Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NULL", null);
                        adapter.changeCursor(cursor);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.w("errorresp", error.toString());
                        //ADD TO DB WITH ERROR
                        Toast.makeText(MainActivity.this, "Spotify Response Error", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + sessionToken);
                return headers;
            }
        };
        requestQueue.add(jsonreq);
    }

    //PERMISSION RESULT
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERM_REQUEST_GPS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            } else {
                //DISABILITA GEOLOCALIZZAZIONE
            }
        }
        if (requestCode == PERM_REQUEST_WREXT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            } else {
                //DISABILITA SCATTARE FOTO
            }
        }
        if (requestCode == PERM_REQUEST_INTERNET) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            } else {
                //DISABILITA USO INTERNET
            }
        }
    }
}

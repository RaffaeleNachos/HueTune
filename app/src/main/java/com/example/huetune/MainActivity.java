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

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;

import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


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
        myCursor = db.rawQuery("SELECT _id,* FROM pics", null);

        final ListView lview = findViewById(R.id.listview);
        adapter = new MyAdapter(this, myCursor);
        lview.setAdapter(adapter);

        //FILTER QUERY FOR SEARCH
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String query = "SELECT _id,* FROM pics "
                        + "where location like '%" + constraint + "%' ";
                return db.rawQuery(query, null);
            }
        });

        //POSITION
        geocoder = new Geocoder(this, Locale.getDefault());
        //testare se geocoder è presente nel paese DA FARE

        //API FOR PLACE AUTOCOMPLETE
        String apiKey = getString(R.string.myapikey);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

    }

    //metod to add search and oth opt
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchvwi = menu.findItem(R.id.action_search);
        searchvw = (SearchView) searchvwi.getActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this, "Action settings clicked", Toast.LENGTH_LONG).show();
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).setCountry("IT").build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
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
            Snackbar sbar = Snackbar.make(findViewById(R.id.myFABLayout), "Deleting all photos", Snackbar.LENGTH_LONG);
            LinearLayout llc = findViewById(R.id.llc);
            LinearLayout llg = findViewById(R.id.llg);
            sbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), "Undo action", Toast.LENGTH_SHORT).show();
                }
            });
            llc.setVisibility(View.INVISIBLE);
            llg.setVisibility(View.INVISIBLE);
            btncam.setClickable(false);
            btngal.setClickable(false);
            if(clickadd) {
                llc.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_close));
                llg.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_exp_close));
            }
            clickadd=false;
            sbar.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode==Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            handler.addPic(imageUri.toString(), "Choose Position", "Song");
            db = handler.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id,* FROM pics", null);
            adapter.changeCursor(cursor);
        }
        if (requestCode == TAKE_IMAGE_REQUEST && resultCode==Activity.RESULT_OK) {
            Uri imageUri = Uri.fromFile(new File(currentPhotoPath));
            Log.w("path cam", currentPhotoPath);
            //get della posizione da fare assolutamente in asynctask
            handler.addPic(imageUri.toString(), getMyPosition(currentPhotoPath), "Song");
            db = handler.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id,* FROM pics", null);
            adapter.changeCursor(cursor);
        }
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Place place = Autocomplete.getPlaceFromIntent(data);
            Log.i("autocomplete", "Place: " + place.getName() + ", " + place.getId());
            String address = place.getAddress();
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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
}

package com.example.huetune;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Boolean clickadd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FAB

        FloatingActionButton btncam = findViewById(R.id.addc);
        final LinearLayout llc = findViewById(R.id.llc);
        btncam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cint = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivity(cint);
            }
        });

        FloatingActionButton btngal = findViewById(R.id.addg);
        final LinearLayout llg = findViewById(R.id.llg);
        btngal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reqImageG();
            }
        });

        FloatingActionButton btnadd = findViewById(R.id.add);
        btnadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickadd) {
                    llc.setVisibility(View.INVISIBLE);
                    llg.setVisibility(View.INVISIBLE);
                    clickadd = false;
                }
                else {
                    llc.setVisibility(View.VISIBLE);
                    llg.setVisibility(View.VISIBLE);
                    clickadd = true;
                }
            }
        });


        //DATABASE
        PicDBHandler handler = new PicDBHandler(this);

        SQLiteDatabase db = handler.getWritableDatabase();
        Cursor myCursor = db.rawQuery("SELECT _id,* FROM pics", null);

        ListView lview = findViewById(R.id.listview);
        MyAdapter adapter = new MyAdapter(this, myCursor);
        lview.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }
        if (id == R.id.action_search) {
            Toast.makeText(MainActivity.this, "Action search  clicked", Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.action_del) {
            Snackbar sbar = Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Deleting all photos", Snackbar.LENGTH_LONG);

            sbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), "Undo action", Toast.LENGTH_SHORT).show();
                }
            });
            sbar.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void reqImageG(){
        int PICK_IMAGE_REQUEST = 1;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_IMAGE_REQUEST);
    }
}

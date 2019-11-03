package com.example.huetune;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;

public class HueBinActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private PicDBHandler handler;
    private MyAdapter adapter;
    private Cursor myCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hue_bin);

        Toolbar bintoolbar = findViewById(R.id.bintoolbar);
        setSupportActionBar(bintoolbar);

        handler = new PicDBHandler(this);
        db = handler.getWritableDatabase();
        myCursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NOT NULL", null);

        ListView binview = findViewById(R.id.binlistview);
        adapter = new MyAdapter(this, myCursor);
        binview.setAdapter(adapter);

        //attivo menu contestuale floating
        registerForContextMenu(binview);
    }

    //inflate menu in toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bin_menu, menu);
        return true;
    }

    //inflate custom menu from listview item
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.bin_actions , menu);
    }

    //select toolbar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.restoreall_b) {
            Snackbar sbar = Snackbar.make(findViewById(R.id.mainbinlay), "All Photos Restored", Snackbar.LENGTH_LONG);
            handler.resumeAllPicsFromBin();
            db = handler.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NOT NULL", null);
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
        Integer itemPosition = info.position;

        int id = item.getItemId();
        if(id == R.id.restore_b) {
            myCursor = adapter.getCursor();
            myCursor.moveToPosition(itemPosition);
            Snackbar sbar = Snackbar.make(findViewById(R.id.mainbinlay), "Photo Restored", Snackbar.LENGTH_LONG);
            handler.resumePicFromBin(myCursor.getString(myCursor.getColumnIndexOrThrow("picuri")));
            db = handler.getWritableDatabase();
            myCursor = db.rawQuery("SELECT _id,* FROM pics WHERE date IS NOT NULL", null);
            adapter.changeCursor(myCursor);
            sbar.show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

}

package sk.besttrailsoft.fat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

public class OptionsActivity extends AppCompatActivity {

    static final int PICK_PLACES_REQUEST = 1;

    private ArrayList<String> places = new ArrayList<String>();
    ArrayAdapter<String> placesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        placesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, places);
        ListView placesListView = (ListView) findViewById(R.id.placesListView);
        placesListView.setAdapter(placesAdapter);
        registerForContextMenu(placesListView);
    }

    public void onStartButtonToMapClick(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("places", places);
        startActivity(intent);
    }

    public void onAddPlaceClick(View view) {
        Intent intent = new Intent(this, AutocompletePlaceActivity.class);
        intent.putExtra("places", places);
        startActivityForResult(intent, PICK_PLACES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_PLACES_REQUEST) {
            places.clear();
            places.addAll(data.getStringArrayListExtra("places"));
            placesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.placesListView) {
            AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(places.get(info.position));
            menu.add(Menu.NONE, 0, 0, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
        places.remove(info.position);
        placesAdapter.notifyDataSetChanged();
        return true;
    }
}

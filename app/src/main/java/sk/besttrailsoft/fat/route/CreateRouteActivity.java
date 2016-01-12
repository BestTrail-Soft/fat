package sk.besttrailsoft.fat.route;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import sk.besttrailsoft.fat.AutocompletePlaceActivity;
import sk.besttrailsoft.fat.R;
import sk.besttrailsoft.fat.program.Program;

public class CreateRouteActivity extends AppCompatActivity {

    static final int PICK_PLACES_REQUEST = 1;
    private final int DELETE_MENU_ITEM = 100;

    private ArrayList<String> places = new ArrayList<>();
    private ArrayAdapter<String> placesAdapter;
    private RouteManager routeManager;

    EditText routeNameView;
    String oldName;
    boolean oldNameExists = false;

    ListView placesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        oldName = intent.getStringExtra("name");
        routeNameView = (EditText) findViewById(R.id.routeNameEditText);
        routeManager = new RouteManager(this.getApplicationContext());

        if(intent.getStringArrayListExtra("places") != null && !(intent.getStringArrayListExtra("places").isEmpty()))
            places = intent.getStringArrayListExtra("places");
        placesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, places);
        placesListView = (ListView) findViewById(R.id.waypointsListView);
        placesListView.setAdapter(placesAdapter);

        placesListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                if (v.getId() == R.id.waypointsListView) {
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                    menu.setHeaderTitle(places.get(info.position));
                    menu.add(Menu.NONE, DELETE_MENU_ITEM, Menu.NONE, "Delete");
                }
            }
        });
        if(oldName != null && !oldName.isEmpty()) {
            try {
                Route route = routeManager.getRoute(oldName);
                oldNameExists = true;
                routeNameView.setText(route.getName());
                places.addAll(route.getWaypoints());
                placesAdapter.notifyDataSetChanged();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
   }

    public void onChangeWaypointsButtonClick(View view) {
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
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        places.remove(info.position);
        placesAdapter.notifyDataSetChanged();
        return true;
    }

    public void onSaveButtonClick(View view) {
        String name = routeNameView.getText().toString();

        if (routeNameView.getText().toString().matches("")){
            Toast.makeText(this, "You did not enter a route name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (places.size() == 0){
            Toast.makeText(this, "You need at least one place", Toast.LENGTH_SHORT).show();
            return;

        }
        Route route = new Route();
        route.setName(name);

        route.setWaypoints(places);
        try {
            if(oldNameExists) {
                routeManager.deleteRoute(oldName);
            }
            routeManager.createRoute(route);
            Intent intent= new Intent();
            setResult( RESULT_OK, intent);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

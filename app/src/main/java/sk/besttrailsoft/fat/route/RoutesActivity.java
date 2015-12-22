package sk.besttrailsoft.fat.route;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import sk.besttrailsoft.fat.R;
import sk.besttrailsoft.fat.program.CreateProgramActivity;
import sk.besttrailsoft.fat.program.Program;
import sk.besttrailsoft.fat.program.ShowProgramActivity;

public class RoutesActivity extends AppCompatActivity {

    private final int DELETE_MENU_ITEM = 1;
    private final int EDIT_MENU_ITEM = 2;
    static final int CREATE_ROUTE_REQUEST = 3;

    private ListView listView;
    private ArrayList<String> names;
    private RouteManager routeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        routeManager = new RouteManager(this.getApplicationContext());

        names = new ArrayList<>(Arrays.asList(routeManager.getAllRoutesNames()));
        listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.emptyElement));

        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                if (v.getId() == R.id.listView) {
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                    menu.setHeaderTitle(names.get(info.position));
                    menu.add(Menu.NONE, EDIT_MENU_ITEM, Menu.NONE, "Edit");
                    menu.add(Menu.NONE, DELETE_MENU_ITEM, Menu.NONE, "Delete");
                }
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch (item.getItemId()){
            case DELETE_MENU_ITEM:
                Toast.makeText(getApplicationContext(), names.get(info.position) + " deleted", Toast.LENGTH_SHORT).show();
                routeManager.deleteRoute(names.get(info.position));
                updateRoutesNames();
                break;
            case EDIT_MENU_ITEM:
                Intent intent = new Intent(this, CreateRouteActivity.class);
                intent.putExtra("name", names.get(info.position));
                startActivityForResult(intent, CREATE_ROUTE_REQUEST);
                break;
        }
        return true;
    }

    public void onCreateRouteButton(View view){
        Intent intent = new Intent(this, CreateRouteActivity.class);
        startActivityForResult(intent, CREATE_ROUTE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == CREATE_ROUTE_REQUEST) {
            updateRoutesNames();
        }
    }

    private void updateRoutesNames(){
        names.clear();
        names.addAll(Arrays.asList(routeManager.getAllRoutesNames()));
        ((ArrayAdapter)listView.getAdapter()).notifyDataSetChanged();
    }

}

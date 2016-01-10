package sk.besttrailsoft.fat.program;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import sk.besttrailsoft.fat.R;
import sk.besttrailsoft.fat.program.CreateProgramActivity;
import sk.besttrailsoft.fat.program.ProgramManager;

public class ProgramsActivity extends AppCompatActivity {

    private final int EDIT_MENU_ITEM = 0;
    private final int DELETE_MENU_ITEM = 1;
    private ListView listView;

    private ArrayList<String> names;
    private ProgramManager programManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);

        if (getSupportActionBar() == null){

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        programManager = new ProgramManager(this.getApplicationContext());

        names = new ArrayList<>(Arrays.asList(programManager.getAllProgramsNames()));
            listView = (ListView) findViewById(R.id.listView);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
            listView.setAdapter(adapter);
            listView.setEmptyView(findViewById(R.id.emptyElement));
           listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position,
                                        long arg3) {
                    String name = (String) adapter.getItemAtPosition(position);
                    Program program;


                        Intent intent = new Intent(getApplicationContext(), ShowProgramActivity.class);
                        intent.putExtra("name", name);
                        startActivity(intent);


                }
            });

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
            case EDIT_MENU_ITEM:
                Intent intent = new Intent(this, CreateProgramActivity.class);
                intent.putExtra("name", names.get(info.position));
                intent.putExtra("edit",true);
                startActivity(intent);
                break;
            case DELETE_MENU_ITEM:
                Toast.makeText(getApplicationContext(), names.get(info.position) + " deleted", Toast.LENGTH_SHORT).show();
                programManager.deleteProgram(names.get(info.position));
                updateProgramsNames();

        }

        return true;
    }

    public void onCreateProgramButton(View view){
        Intent intent = new Intent(this, CreateProgramActivity.class);

        startActivity(intent);

    }

    private void updateProgramsNames(){
        names.clear();
        names.addAll(Arrays.asList(programManager.getAllProgramsNames()));


        ((ArrayAdapter)listView.getAdapter()).notifyDataSetChanged();
    }
}

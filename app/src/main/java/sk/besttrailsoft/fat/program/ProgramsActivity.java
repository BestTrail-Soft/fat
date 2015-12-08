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

import java.util.ArrayList;
import java.util.Arrays;

import sk.besttrailsoft.fat.R;
import sk.besttrailsoft.fat.program.CreateProgramActivity;
import sk.besttrailsoft.fat.program.ProgramManager;

public class ProgramsActivity extends AppCompatActivity {

    private final int EDIT_MENU_ITEM = 0;
    private final int DELETE_MENU_ITEM = 1;
    private ListView listView;
    //private String[] data = {"1","2","3","4","5"};
    private ArrayList<String> names;
    private ProgramManager programManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        programManager = new ProgramManager(this.getApplicationContext());

        names = new ArrayList<>(Arrays.asList(programManager.getAllProgramsNames()));
            listView = (ListView) findViewById(R.id.listView);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
            listView.setAdapter(adapter);
            listView.setEmptyView(findViewById(R.id.emptyElement));
           /* listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position,
                                        long arg3) {
                    String value = (String) adapter.getItemAtPosition(position);
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete entry")
                            .setMessage("Are you sure you want to delete this entry?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });*/

        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                if (v.getId() == R.id.listView) {
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                    menu.setHeaderTitle(names.get(info.position));
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
        Toast.makeText(getApplicationContext(), "DELETE", Toast.LENGTH_SHORT).show();
    }






}

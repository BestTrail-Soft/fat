package sk.besttrailsoft.fat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sk.besttrailsoft.fat.program.ProgramManager;

public class OptionsActivity extends AppCompatActivity {

    static final int PICK_PLACES_REQUEST = 1;

    private ArrayList<String> places = new ArrayList<String>();
    ArrayAdapter<String> placesAdapter;
    private String trainingProgram = null;

    private CheckBox trainingInstructionsCheckBox = null;
    private TextView trainingInstructionsTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        trainingInstructionsCheckBox = (CheckBox) findViewById(R.id.trainingInstructionsCheckBox);
        trainingInstructionsTextView = (TextView) findViewById(R.id.trainingInstructionsTextView);

        trainingInstructionsCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    if (isChecked)
                                                        onTrainingInstructionChecked();
                                                    else
                                                        onTrainingInstructionUnchecked();
                                                }
                                            }
                );
        placesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, places);
        ListView placesListView = (ListView) findViewById(R.id.placesListView);
        placesListView.setAdapter(placesAdapter);
        registerForContextMenu(placesListView);
    }

    public void onStartButtonToMapClick(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("places", places);
        intent.putExtra("program", trainingProgram);
        startActivity(intent);
    }

    public void onAddPlaceClick(View view) {
        Intent intent = new Intent(this, AutocompletePlaceActivity.class);
        intent.putExtra("places", places);
        startActivityForResult(intent, PICK_PLACES_REQUEST);
    }

    public void onTrainingInstructionUnchecked() {
        trainingInstructionsTextView.setText("None");
        trainingProgram = null;
    }

    public void onTrainingInstructionChecked() {
        final String[] programs = new ProgramManager(getApplicationContext()).getAllProgramsNames();
        if(programs != null || programs.length > 0) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(OptionsActivity.this);
            builderSingle.setTitle("Choose program");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    OptionsActivity.this,
                    android.R.layout.select_dialog_singlechoice);
            arrayAdapter.addAll(programs);

            builderSingle.setNegativeButton(
                    "cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            trainingInstructionsCheckBox.setChecked(false);
                            dialog.dismiss();
                        }
                    });

            builderSingle.setAdapter(
                    arrayAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //String strName = arrayAdapter.getItem(which);
                            //AlertDialog.Builder builderInner = new AlertDialog.Builder(
                            //        OptionsActivity.this);
                            //builderInner.setMessage(strName);
                            //builderInner.setTitle("Your Selected Item is");
                            //builderInner.setPositiveButton(
                            //        "Ok",
                            //        new DialogInterface.OnClickListener() {
                            //            @Override
                            //            public void onClick(
                            //                    DialogInterface dialog,
                            //                    int which) {
                            //                dialog.dismiss();
                            //            }
                            //        });
                            //builderInner.show();
                            trainingInstructionsTextView.setText(programs[which]);
                            trainingProgram = programs[which];
                        }
                    });
            builderSingle.show();
        }
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

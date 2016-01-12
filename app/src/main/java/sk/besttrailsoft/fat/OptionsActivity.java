package sk.besttrailsoft.fat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sk.besttrailsoft.fat.program.ProgramManager;
import sk.besttrailsoft.fat.route.RouteManager;

public class OptionsActivity extends AppCompatActivity {

    static final int PICK_PLACES_REQUEST = 1;

    private ArrayList<String> places = new ArrayList<>();
    ArrayAdapter<String> placesAdapter;
    private String trainingProgram = null;

    private CheckBox trainingInstructionsCheckBox = null;
    private TextView trainingInstructionsTextView = null;
    private CheckBox defineRouteCheckBox = null;
    private TextView routeTextView = null;
    private Button defineRouteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_options);
        if (getSupportActionBar() == null){

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        trainingInstructionsCheckBox = (CheckBox) findViewById(R.id.trainingInstructionsCheckBox);
        trainingInstructionsCheckBox.setEnabled(new ProgramManager(getApplicationContext()).programsExist());
        trainingInstructionsTextView = (TextView) findViewById(R.id.trainingInstructionsTextView);
        defineRouteCheckBox = (CheckBox) findViewById(R.id.defineRouteCheckBox);
        defineRouteCheckBox.setEnabled(new RouteManager(getApplicationContext()).doPredefinedRoutesExist());
        routeTextView = (TextView) findViewById(R.id.textView2);
        defineRouteButton = (Button) findViewById(R.id.button2);
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
        placesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, places);
        ListView placesListView = (ListView) findViewById(R.id.placesListView);
        placesListView.setAdapter(placesAdapter);
        defineRouteCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    if (isChecked)
                                                        onDefinedRouteChecked();
                                                    else
                                                        onDefineRouteUnchecked();
                                                }
                                            }
                );
        registerForContextMenu(placesListView);
        TextView mockText = (TextView)findViewById(R.id.mockText);
        SharedPreferences settings = getSharedPreferences("mock_settings", 0);

        if (settings.getBoolean("enabledMock", false)){
            mockText.setText("(mock enabed)");
        }
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
        if(programs != null && programs.length > 0) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(OptionsActivity.this);
            builderSingle.setTitle("Choose program");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
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
            findViewById(R.id.button).setEnabled(true);
            ArrayList<String> newPlaces = data.getStringArrayListExtra("places");
            if (newPlaces.isEmpty() || newPlaces.get(0) == null || newPlaces.get(0).isEmpty() ){
                routeTextView.setText("Free Trail");
                places.addAll(new ArrayList<String>());
            }
            else{
                if (newPlaces.size() == 1)
                    findViewById(R.id.button).setEnabled(false);


                routeTextView.setText("Defined Route");
                places.addAll(newPlaces);
            }

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

    public void onDefineRouteUnchecked() {
        defineRouteButton.setEnabled(true);
        routeTextView.setText("Free trail");
        reloadPlacesList(new ArrayList<String>());
    }

    public void onDefinedRouteChecked() {
        defineRouteButton.setEnabled(false);
        final RouteManager routeManager = new RouteManager(getApplicationContext());
        final String[] routes = routeManager.getAllRoutesNames();
        if(routes != null && routes.length > 0) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(OptionsActivity.this);
            builderSingle.setTitle("Choose your Route");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    OptionsActivity.this,
                    android.R.layout.select_dialog_singlechoice);
            arrayAdapter.addAll(routes);

            builderSingle.setNegativeButton(
                    "cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            defineRouteCheckBox.setChecked(false);
                            dialog.dismiss();
                        }
                    });

            builderSingle.setAdapter(
                    arrayAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                ArrayList<String> places = new ArrayList<>();
                                places.addAll(routeManager.getRoute(routes[which]).getWaypoints());
                                reloadPlacesList(places);
                                routeTextView.setText(routes[which]);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
            builderSingle.show();
        }
    }

    private void reloadPlacesList(ArrayList<String> places) {
        this.places.clear();
        findViewById(R.id.button).setEnabled(true);
        if (places.size()==1)
            findViewById(R.id.button).setEnabled(false);
        this.places.addAll(places);
        placesAdapter.notifyDataSetChanged();
    }
}

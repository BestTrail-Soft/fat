package sk.besttrailsoft.fat.program;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;

import sk.besttrailsoft.fat.R;

public class CreateProgramActivity extends AppCompatActivity implements IContextMenuable {

    private ListView listView;
    private ArrayList<ProgramStep> data = new ArrayList<>();
    private EditText programNameView;
    private ListView programStepsView;
    private EditText newStepText;
    private ProgramManager programManager;
    private Toast toast;
    private boolean toastShown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_program);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        programNameView = (EditText) findViewById(R.id.programNameEditText);
        programStepsView = (ListView) findViewById(R.id.stepsListView);



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listView = (ListView) findViewById(R.id.stepsListView);
        listView.setAdapter(new DragAndDropStepsAdapter(this, data));
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                if (v.getId() == R.id.stepsListView) {
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

                    menu.add(Menu.NONE, 1, Menu.NONE, "Delete");
                }
            }


        });


        programManager = new ProgramManager(this.getApplicationContext());
    }

    public void onAddStepButtonClick(final View view) {
        final String[] duration = {"distance","time"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View inflator = this.getLayoutInflater().inflate(R.layout.create_step_dialog, null);
        final TextView durationUnitsTextView = (TextView) inflator.findViewById(R.id.durationUnitsTextView);
        final EditText durationEditTextView = (EditText) inflator.findViewById(R.id.durationEditTextView);
        final Spinner durationSpinner = (Spinner) inflator.findViewById(R.id.duration_spinner);
                durationSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, duration));
        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (duration[position]) {
                    case "distance":
                        durationUnitsTextView.setText("meter(s)");
                        break;
                    case "time":
                        durationUnitsTextView.setText("minute(s)");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        builder.setTitle("New Step")
                .setView(inflator)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        newStepText = (EditText) inflator.findViewById(R.id.newStepeditText);
                        if (newStepText.getText().toString().matches("")) {
                            Toast.makeText(view.getContext(), "You did not enter any text", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ProgramStep step = new ProgramStep();
                        step.setText(newStepText.getText().toString());
                        if (duration[durationSpinner.getSelectedItemPosition()] == "distance") {
                            step.setDistance(Integer.parseInt(durationEditTextView.getText().toString()));
                        } else {
                            step.setTime(Integer.parseInt(durationEditTextView.getText().toString()));
                        }


                        data.add(step);
                        ((DragAndDropStepsAdapter) listView.getAdapter()).notifyDataSetChanged();
                        listView.setSelection(((DragAndDropStepsAdapter) listView.getAdapter()).getCount() - 1);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Cancel button
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        /**/
    }

    public void onSaveButtonClick(View view) {
        String name = programNameView.getText().toString();

        if (programNameView.getText().toString().matches("")){
            toast = Toast.makeText(this, "You did not enter a program name", Toast.LENGTH_SHORT);
            toast.show();
            return;

        }

        if (data.size() == 0){
            Toast.makeText(this, "You need at least one step", Toast.LENGTH_SHORT).show();
            return;

        }

        Program program = new Program();
        program.setName(name);


        program.setSteps(data);
        try {

            programManager.createProgram(program);
            NavUtils.navigateUpFromSameTask(this);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch (item.getItemId()){

            case 1:
                Toast.makeText(getApplicationContext(), "Step deleted", Toast.LENGTH_SHORT).show();
                data.remove(info.position);
                ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();

        }

        return true;
    }

    @Override
    public void openListViewMenu(View view) {
       openContextMenu(listView);

    }

    public String getToastText() {

        String displayedText = ((TextView)((LinearLayout)toast.getView()).getChildAt(0)).getText().toString();
        return displayedText;

    }
}

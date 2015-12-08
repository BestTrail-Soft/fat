package sk.besttrailsoft.fat.program;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;

import sk.besttrailsoft.fat.R;

public class CreateProgramActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<ProgramStep> data = new ArrayList<>();
    EditText programNameView;
    ListView programStepsView;
    EditText newStepText;
    ProgramManager programManager;

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
        programManager = new ProgramManager(this.getApplicationContext());
    }

    public void onAddStepButtonClick(final View view) {
        String[] duration = {"distance","time"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View inflator = this.getLayoutInflater().inflate(R.layout.create_step_dialog, null);
        ((Spinner)inflator.findViewById(R.id.duration_spinner)).setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, duration));
        builder.setTitle("New Step")
                .setView(inflator)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        newStepText = (EditText) inflator.findViewById(R.id.newStepeditText);
                        if (newStepText.getText().toString().matches("")){
                            Toast.makeText(view.getContext(), "You did not enter any text", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ProgramStep step = new ProgramStep();
                        step.setText(newStepText.getText().toString());
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
            Toast.makeText(this, "You did not enter a program name", Toast.LENGTH_SHORT).show();
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
}

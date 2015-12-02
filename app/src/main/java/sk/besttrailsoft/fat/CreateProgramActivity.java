package sk.besttrailsoft.fat;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

import sk.besttrailsoft.fat.program.Program;
import sk.besttrailsoft.fat.program.ProgramManager;
import sk.besttrailsoft.fat.program.ProgramStep;

public class CreateProgramActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> data = new ArrayList<>();
    EditText programNameView;
    ListView programStepsView;
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
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data));
        programManager = new ProgramManager(this.getApplicationContext());
    }

    public void onAddStepButton(View view) {
        data.add("new step");
        ((ArrayAdapter<Object>) listView.getAdapter()).notifyDataSetChanged();
        listView.setSelection(((ArrayAdapter<Object>) listView.getAdapter()).getCount() - 1);
    }

    public void onSaveButtonClick(View view) {
        String name = programNameView.getText().toString();
        if (name == null || name == "")
            return;
        Program program = new Program();
        program.setName(name);
        ProgramStep step = new ProgramStep();
        step.setText(data.get(0));
        ArrayList<ProgramStep> steps = new ArrayList<>();
        steps.add(step);
        program.setSteps(steps);
        try {

            programManager.createProgram(program);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

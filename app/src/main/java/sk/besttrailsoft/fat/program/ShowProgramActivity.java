package sk.besttrailsoft.fat.program;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;

import sk.besttrailsoft.fat.R;

public class ShowProgramActivity extends AppCompatActivity implements IContextMenuable{

    private Program program;
    private TextView programNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_program);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        programNameView = (TextView) findViewById(R.id.programNameTextView);

        ProgramManager manager = new ProgramManager(getApplicationContext());
        try {
            program = manager.getProgram(getIntent().getStringExtra("name"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        programNameView.setText(program.getName());
        ListView listView = (ListView) findViewById(R.id.stepsListView);
        listView.setAdapter(new DragAndDropStepsAdapter(this, program.getSteps()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void openListViewMenu(View v) {

    }
}

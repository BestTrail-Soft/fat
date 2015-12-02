package sk.besttrailsoft.fat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONObject;

import sk.besttrailsoft.fat.program.ProgramManager;

public class ProgramActivity extends AppCompatActivity {

    private ListView listView;
    private String[] data = {"1","2","3","4","5"};
    private ProgramManager programManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //          .setAction("Action", null).show();

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        programManager = new ProgramManager(this.getApplicationContext());
        String[] names = programManager.getAllProgramsNames();
        listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);

    }

    public void onButtonClick(View view){
        /*FileHelper helper = FileHelper.getInstance(view.getContext());
        try{
            helper.writeToFile("test","Hello pán Kocúr!", "programs");

        }
        catch(Exception ex){
            System.err.println(ex);
        }*/



        /*Button b = (Button) findViewById(R.id.button3);
        b.setText("Done");*/
    }

    public void onCreateProgramButton(View view){
        Intent intent = new Intent(this, CreateProgramActivity.class);
        startActivity(intent);
    }


}

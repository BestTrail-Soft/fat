package sk.besttrailsoft.fat.program;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;

import sk.besttrailsoft.fat.R;

public class ShowProgramActivity extends AppCompatActivity implements IContextMenuable{

    private Program program;
    private TextView programNameView;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    FrameLayout progressBarHolder;
    ProgramManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_program);
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        programNameView = (TextView) findViewById(R.id.programNameTextView);

        manager = new ProgramManager(getApplicationContext());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        new MyTask().execute();
    }


    @Override
    public void openListViewMenu(View v) {

    }

    private class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setContent();
                }
            });

        }

        @Override
        protected Void doInBackground(Void... params) {

                try {
                    program = manager.getProgram(getIntent().getStringExtra("name"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }




            return null;
        }
    }

    private void setContent(){
        programNameView.setText(program.getName());
        ListView listView = (ListView) findViewById(R.id.stepsListView);
        listView.setAdapter(new DragAndDropStepsAdapter(this, program.getSteps()));
    }
}

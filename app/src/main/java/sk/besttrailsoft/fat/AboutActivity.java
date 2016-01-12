package sk.besttrailsoft.fat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onMockClick(View view) {
        SharedPreferences settings = getSharedPreferences("mock_settings", MODE_PRIVATE);
        boolean mockEnabled = settings.getBoolean("enabledMock", false);
                mockEnabled = ! mockEnabled;

        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("enabledMock", mockEnabled);

        editor.commit();

        String s = "DISABLED";
        if (mockEnabled)
            s="ENABLED";
        Toast.makeText(getApplicationContext(), "MOCK " + s, Toast.LENGTH_SHORT).show();
    }

    public void onAdrianaClick(View view) {
        Toast.makeText(getApplicationContext(), "MŇAU!", Toast.LENGTH_SHORT).show();
    }

    public void onMarosClick(View view) {
        Toast.makeText(getApplicationContext(), "PUF! PAC!", Toast.LENGTH_SHORT).show();
    }
}

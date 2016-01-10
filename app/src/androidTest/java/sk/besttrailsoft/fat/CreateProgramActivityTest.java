package sk.besttrailsoft.fat;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.Assert;

import sk.besttrailsoft.fat.program.CreateProgramActivity;

/**
 * Created by Mros on 1/10/16.
 */
public class CreateProgramActivityTest extends ActivityInstrumentationTestCase2<CreateProgramActivity> {
    private CreateProgramActivity activity;
    private Button button;


    public CreateProgramActivityTest() {
        super(CreateProgramActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();


    }

    /**
     * Test if your test fixture has been set up correctly. You should always implement a test that
     * checks the correct setup of your test fixture. If this tests fails all other tests are
     * likely to fail as well.
     */
    public void testPreconditions() {


        button = (Button) activity.findViewById(R.id.saveButton);
        //Try to add a message to add context to your assertions. These messages will be shown if
        //a tests fails and make it easy to understand why a test failed
        assertNotNull("activity is null", activity);
        assertNotNull("button is null", button);
    }

    /**
     * Tests the correctness of the initial text.
     */
    public void testToastWithoutProgramTitle() {
        button = (Button) activity.findViewById(R.id.saveButton);

        TouchUtils.clickView(this, button);

        assertEquals("toast test is incorrect", activity.getToastText(), "You did not enter a program name");

    }




}
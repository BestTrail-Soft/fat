package sk.besttrailsoft.fat;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.ContextThemeWrapper;
import android.widget.Button;

import junit.framework.TestResult;

import sk.besttrailsoft.fat.program.ProgramsActivity;

/**
 * Created by Mros on 1/10/16.
 */
public class ProgramsActivityTest extends ActivityUnitTestCaseWithoutBug<ProgramsActivity> {


    private Intent intent;

    public ProgramsActivityTest() {
        super(ProgramsActivity.class);
    }



    @Override
    protected void setUp() throws Exception{
        super.setUp();
        intent = new Intent(getInstrumentation().getTargetContext(),
                ProgramsActivity.class);
    }

    @Override
    protected void setActivity(Activity testActivity) {
        if (testActivity != null) testActivity.setTheme(R.style.AppTheme);
        super.setActivity(testActivity);
    }



    @MediumTest
    public void testPreconditions() {
        //Start the activity under test in isolation, without values for savedInstanceState and
        //lastNonConfigurationInstance
        startActivity(intent, null, null);
        final Button launchNextButton = (Button) getActivity().findViewById(R.id.button3);

        assertNotNull("activity is null", getActivity());
        assertNotNull("button is null", launchNextButton);
    }


    public void testCreateProgramActivityStart(){
        startActivity(intent, null, null);
        final Button launchNextButton = (Button) getActivity().findViewById(R.id.button3);
        //Because this is an isolated ActivityUnitTestCase we have to directly click the
        //button from code
        launchNextButton.performClick();

        // Get the intent for the next started activity
        final Intent launchIntent = getStartedActivityIntent();
        //Verify the intent was not null.
        assertNotNull("Intent was null", launchIntent);

    }
}

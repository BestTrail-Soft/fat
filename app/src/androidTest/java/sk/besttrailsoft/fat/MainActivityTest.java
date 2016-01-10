package sk.besttrailsoft.fat;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import junit.framework.Assert;

import org.junit.Test;

import java.security.InvalidParameterException;

/**
 * Created by Mros on 1/10/16.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mainActivity;
    private TextView title;
    private final String TITLE = "Fitness Trail Assistant";

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mainActivity = getActivity();
        title = (TextView) mainActivity.findViewById(R.id.textView);

    }

    /**
     * Test if your test fixture has been set up correctly. You should always implement a test that
     * checks the correct setup of your test fixture. If this tests fails all other tests are
     * likely to fail as well.
     */
    public void testPreconditions() {
        //Try to add a message to add context to your assertions. These messages will be shown if
        //a tests fails and make it easy to understand why a test failed
        assertNotNull("mainActivity is null", mainActivity);
        assertNotNull("title is null", title);
    }

    /**
     * Tests the correctness of the initial text.
     */
    public void testTitleContent() {


        final String actual = title.getText().toString();
        assertEquals("Title contains wrong text", TITLE, actual);
    }
}
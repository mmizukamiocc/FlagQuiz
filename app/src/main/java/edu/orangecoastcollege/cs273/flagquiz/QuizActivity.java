package edu.orangecoastcollege.cs273.flagquiz;

import android.content.SharedPreferences;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences.Editor;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.*;

import static android.R.attr.key;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static edu.orangecoastcollege.cs273.flagquiz.R.id.quizFragment;


public class QuizActivity extends AppCompatActivity {

    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";

    private boolean phoneDevice = true; // used to force portrait mode
    private boolean preferencesChanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set default values in the app's SharedPreference
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


        //register listener for SharedPreference change
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(preferencesChangeListener);

        // determine screen size
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;


        //if device is a tablet, set phoneDevice to false
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            phoneDevice = false; // not a phone sized device

        if (phoneDevice)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChanged) {

            // now that the default preference have been set,
            // initialize QuizActivityFragment and start the quiz
            QuizActivityFragment quizFragment = (QuizActivityFragment)
                    getSupportFragmentManager().findFragmentById(R.id.quizFragment);

            quizFragment.updateGuessRows(
                    PreferenceManager.getDefaultSharedPreferences(this));

            quizFragment.updateRegions(
                    PreferenceManager.getDefaultSharedPreferences(this));

            quizFragment.resetQuiz();
            preferencesChanged = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        int orientation = getResources().getConfiguration().orientation;


        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            getMenuInflater().inflate(R.menu.menu_quiz, menu);
            return true;
        } else
            return false;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent preferencesIntent = new Intent(this, SettingActivity.class);
        startActivity(preferencesIntent);

        return super.onOptionsItemSelected(item);
    }

    private OnSharedPreferenceChangeListener preferencesChangeListener =
            new OnSharedPreferenceChangeListener() {
                @Override

                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    preferencesChanged = true;

                    QuizActivityFragment quizFragment = (QuizActivityFragment)
                            getSupportFragmentManager().findFragmentById(R.id.quizFragment);

                    if (key.equals(CHOICES)) {
                        quizFragment.updateGuessRows(sharedPreferences);
                        quizFragment.resetQuiz();
                    }
                else if(key.equals(REGIONS))
                {
                    Set<String> regions =
                            sharedPreferences.getStringSet(REGIONS, null);

                    if (regions != null && regions.size() > 0) {
                        quizFragment.updateRegions(sharedPreferences);
                        quizFragment.requestQuiz();
                    } else {
                        SharedPreferences.Editor editor =
                                sharedPreferences.edit();
                        regions.add(getString(R.string.default_region));
                        editor.putStringSet(REGIONS, regions);
                        editor.apply();

                        Toast.makeText(QuizActivity.this, R.string.default_region_message, Toast.LENGTH_SHORT).show();
                    }
                }

                Toast.makeText(QuizActivity.this,R.string.restarting_quiz,Toast.LENGTH_SHORT).

                show();
                }
            };
}

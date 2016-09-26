package edu.orangecoastcollege.cs273.flagquiz;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.security.SecureRandom;
import java.util.logging.Handler;

import java.util.zip.Inflater;


/**
 * A placeholder fragment containing a simple view.
 */
public class QuizActivityFragment extends Fragment {

    private static final String TAG = "FlagQuiz Activity";

    private static final int FLAGS_IN_QUIZ = 10;

    private List<String> fileNameList;
    private List<String> quizCountriesList;
    private Set<String> regionsSet;
    private String correctAnswer;
    private int totalGuesses;
    private int correctAnswers;
    private int guessRows;
    private SecureRandom random;
    private Handler handler;

    private TextView questionNumberTextView;
    private ImageView flagImageView;
    private LinearLayout[] guessLinearLayout;
    private TextView answerTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.OnCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        fileNameList = new ArrayList<>();
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        questionNumberTextView =
                (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayout = new LinearLayout[4];

        guessLinearLayout[0] =
                (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayout[1] =
                (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayout[2] =
                (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayout[3] =
                (LinearLayout) view.findViewById(R.id.row4LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        for (LinearLayout row : guessLinearLayout) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }
        questionNumberTextView.setText(
                getString(R.string.question, 1, FLAGS_IN_QUIZ));

        return view;
    }

    public void updateGuessRows(SharedPreferences sharedPreferences){

        String choices =
                sharedPreferences.getString(QuizActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        for (LinearLayout layout: guessLinearLayout)
            layout.setVisibility(View.GONE);

        for(int row = 0; row <guessRows;row++)
            guessLinearLayout[row].setVisibility(View.VISIBLE);
    }

    public void updateRegions(SharedPreferences sharedPreferences) {
        regionsSet =
                sharedPreferences.getStringSet(QuizActivity.REGIONS,null);
    }
    public void resetQuiz(){

        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();

        try {
            for (String region : regionsSet) {
                String[] paths = assets.list(region);

                for (String path : paths)
                    fileNameList.add(path.replace(".png", ""));
            }
        }
        catch(IOException exception){
        Log.e(TAG,"Error loading image file names",exception);

        }


        correctAnswers =0;
        totalGuesses =0;
        quizCountriesList.clear();

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        while(flagCounter<= FLAGS_IN_QUIZ){
            int randomIndex = random.nextInt(numberOfFlags);

            String filename= fileNameList.get(randomIndex);

            if (!quizCountriesList.contains(filename)){
                quizCountriesList.add(filename);
                ++flagCounter;
            }
        }

        loadNextFlag();
    }

    private void loadNextFlag(){

        String nextImage = quizCountriesList.remove(0);
        correctAnswer = nextImage;
        answerTextView.setText("");

        questionNumberTextView.setText(getString(R.string.question,(correctAnswers+1),FLAGS_IN_QUIZ));

        String region = nextImage.substring(0,nextImage.indexOf('-'));

        AssetManager assets = getActivity().getAssets();


        try(InputStream stream =
        assets.open(region + "/" + nextImage +".ping"))
        {
            Drawable flag = Drawable.createFromStream(stream,nextImage);
            flagImageView.setImageDrawable(flag);
        }
        catch (IOException exception)
        {
            Log.e(TAG,"Error loading " + nextImage,exception);
        }

        Collections.shuffle(fileNameList);



    }

}
package edu.orangecoastcollege.cs273.flagquiz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;



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
        super.onCreateView(inflater, container, savedInstanceState);

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

    public String getCountryName(String filename)
    {
        return  filename.replace(".png","");
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

        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        for (int row = 0;row <guessRows; row++)
        {
            for(int column = 0; column<guessLinearLayout[row].getChildCount();column++)
            {
                Button newGuessButton = (Button) guessLinearLayout[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                String filename = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getCountryName(filename));

            }

        }

        int row = random.nextInt(guessRows);
        int column = random.nextInt(2);
        LinearLayout randomRow = guessLinearLayout[row];
        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);


    }


    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
    @Override
    public void onClick(View v){
        Button guessButton = ((Button) v);
        String guess = guessButton.getText().toString();
        String answer = getCountryName(correctAnswer);
        ++totalGuesses;

    if (guess.equals(answer)) {
        ++correctAnswers;

        answerTextView.setText(answer+ " ");
        answerTextView.setTextColor(getResources().getColor(R.color.correct_answer,getContext().getTheme()));

        disableButtons();

        if (correctAnswers == FLAGS_IN_QUIZ) {

            DialogFragment quizResults =
                    new DialogFragment(){
                        @Override
                        public Dialog onCreateDialog(Bundle bundle) {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(getActivity());
                            builder.setMessage(getString(R.string.results,
                                    totalGuesses, (1000 / (double) totalGuesses)));

                            builder.setPositiveButton(R.string.reset_quiz,
                                    new DialogInterface.OnClickListener(){
                                public void onClick (DialogInterface dialog,int id){
                                resetQuiz();

                            }
                            }

                            );
           return builder.create();
                        }

            };
        }


    }


    }




    };
}
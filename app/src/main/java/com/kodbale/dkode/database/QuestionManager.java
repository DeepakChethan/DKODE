package com.kodbale.dkode.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * Created by sagar on 2/25/18.
 */

public class QuestionManager {

    public static final String TAG = "QuestionManager";
    private static DatabaseHelper mDatabaseHelper;
    private static QuestionManager mQuestionManager = null;
    private static ArrayList<Question> mNotAnsweredList = null, mAnsweredList = null;
    private Context mAppContext;
    private SharedPreferences mSharedPref;

    private QuestionManager(Context context) {
        mAppContext = context;
        mDatabaseHelper = new DatabaseHelper(mAppContext);
        mNotAnsweredList = new ArrayList<>();
        mAnsweredList = new ArrayList<>();
        mSharedPref = mAppContext.getSharedPreferences("app_data", Context.MODE_PRIVATE);
    }

    public static QuestionManager get(Context c) {
        if(mQuestionManager == null) {
            mQuestionManager = new QuestionManager(c);
        }
        return mQuestionManager;
    }

    public int deleteFromNotAnswered(int index) {
        if(mNotAnsweredList == null || mNotAnsweredList.size() == 0) {
            return 0 ;
        }
        mNotAnsweredList.remove(index);
        return 1;

    }

    public void deleteAllRows() {

        mDatabaseHelper.deleteAllRows();

    }

    public int insertIntoNotAnswered(Question question) {
       if(mNotAnsweredList == null) {
           return 0;
       }
       mNotAnsweredList.add(question);
       return 1;
    }

    public int insertIntoAnswered(Question question) {
        if(mAnsweredList == null) {
            return 0;
        }
        mAnsweredList.add(question);
        return 1;
    }

    public ArrayList<Question> getNotAnsweredList() {
        return mNotAnsweredList;
    }

    public ArrayList<Question> getAnsweredList() {
        return mAnsweredList;
    }

    public void insertQuestion(Question question) {
        Log.i("what's", "called");
        mDatabaseHelper.insertQuestion(question);
    }

    public ArrayList<Question> getAllQuestions() {
        DatabaseHelper.QuestionCursor mQuestionCursor = mDatabaseHelper.queryQuestions();
        ArrayList<Question> questionsList = new ArrayList<>();
        if(mQuestionCursor.getCount() != 0) {
            mQuestionCursor.moveToFirst();
                while (!mQuestionCursor.isAfterLast()) {
                    Question dishItem = mQuestionCursor.getQuestion();
                    questionsList.add(dishItem);
                    mQuestionCursor.moveToNext();
                }
            mQuestionCursor.close();
        }
        return questionsList;
    }

    public void insertAllQuestions() {

        Log.i("i", "in inserting function");

        boolean isInsertedBefore = mSharedPref.getString("inserted_before", "0").equals("0") ? false: true;

        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();


        String savedUser = mSharedPref.getString("current_user", null);

        if(savedUser == null || (!currentUser.equals(savedUser))) {
            mQuestionManager.deleteAllRows();
            mQuestionManager.insertQuestion(new Question("That's it1?", "answer not revealed", 123,false,0));
            mQuestionManager.insertQuestion(new Question("That's it2?", "answer not revealed", 124,false,0));
            mQuestionManager.insertQuestion(new Question("That's it3?", "answer not revealed", 125,false,0));
            mQuestionManager.insertQuestion(new Question("That's it4?", "answer not revealed", 126,false,0));
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString("inserted_before", "1");
            editor.putString("current_user", currentUser);
            editor.apply();
        }
        else {
            Log.i("i", "it was inserted already");
        }
        Log.i("i", currentUser + "is user in");

    }


    public  void initializeFromFirebaseList() {

        ArrayList<Question> questionsList = getAllQuestions();
        ArrayList<Integer> questionIds = new ArrayList<>() ;
        for(int i = 0; i < questionsList.size(); i++) {
            questionIds.add(questionsList.get(i).getQuestionId());
        }

        ArrayList<Integer> answeredListIds = StatusManager.get(mAppContext).getAnsweredListIds();
        mNotAnsweredList = questionsList;

        System.out.println( "sizeof answerded after fetch" + answeredListIds.size());
        System.out.println("Size of answered list " + mAnsweredList.size());


        for(int i = 0; i < answeredListIds.size(); i++) {
            if(questionIds.contains(answeredListIds.get(i))) {
                int indexOfQuestion = getQuestionById(questionsList, answeredListIds.get(i));
                if(indexOfQuestion != -1) {
                    mAnsweredList.add(questionsList.get(indexOfQuestion));
                    mNotAnsweredList.remove(questionsList.get(indexOfQuestion));
                }
            }
        }
        System.out.println("size of not answered " + mNotAnsweredList.size());
        System.out.println("Size of answered list " + mAnsweredList.size());
        System.out.println("sizeof notanswerded" + mNotAnsweredList.size());
    }

    public int getQuestionById(ArrayList<Question> questionList, int questionId) {
        System.out.println("called with" + questionId);
        for(int i = 0; i < questionList.size(); i++) {
            if(questionList.get(i).getQuestionId() == questionId) {
                return i;
            }
        }
        return -1;
    }

    public void setAllToNull() {
        mAnsweredList = null;
        mNotAnsweredList = null;
        mQuestionManager = null;
    }


    public void initializeNotAnsweredList() {
        DatabaseHelper.QuestionCursor mQuestionCursor = mDatabaseHelper.queryNotAnswered();
        Log.i("d", "size of not answered list" + mQuestionCursor.getCount());
        mQuestionCursor.moveToFirst();
        if(mQuestionCursor.getCount() != 0) {
            for(int i = 0; i < mQuestionCursor.getCount(); i++) {
               Question question = mQuestionCursor.getQuestion();
                Log.i("i", "adding to not answeredlist");
                mNotAnsweredList.add(question);
                mQuestionCursor.moveToNext();
            }
        }
        mQuestionCursor.close();
    }



    public void initializeAnsweredList() {
        DatabaseHelper.QuestionCursor mQuestionCursor = mDatabaseHelper.queryAnswered();
        mQuestionCursor.moveToFirst();
        if(mQuestionCursor.getCount() != 0) {
            for(int i = 0; i < mQuestionCursor.getCount(); i++) {
                Question question = mQuestionCursor.getQuestion();
                mAnsweredList.add(question);
                mQuestionCursor.moveToNext();
            }
        }
        mQuestionCursor.close();
    }

    public Question getNextQuestion() {
        if( mNotAnsweredList.size() == 0) {
           return null;
        }
        Log.i("d", "size before" + mNotAnsweredList.size());
        Question question = mNotAnsweredList.get(0);
        mAnsweredList.add(question);
        mNotAnsweredList.remove(0);
        Log.i("d", "size after" + mNotAnsweredList.size() + question.getQuestionText());
        return question;
    }

    public void updateQuestionScoreInDb(long id, int score) {
        mDatabaseHelper.updateQuestionScore(id, score);
    }

    public void updateAnsweredStatusInDb() {
        int id = StatusManager.get(mAppContext).getCurrentQuestion().getQuestion().getQuestionId();
        mDatabaseHelper.updateAnsweredStatus(id) ;
    }

    public void updateNumberOfTries() {
        int numberOfTries = StatusManager.get(mAppContext).getCurrentQuestion().getQuestion().getNumberOfTries();
        int id = StatusManager.get(mAppContext).getCurrentQuestion().getQuestion().getQuestionId();
        mDatabaseHelper.updateNumberOfTries(id, numberOfTries);
    }



}

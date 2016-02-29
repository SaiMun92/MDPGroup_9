package com.example.android.mdpandroid;

/**
 * Created by ngshuling on 21/9/15.
 */


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;

public class SetPreferenceActivity extends Activity
{
    private EditText prefEditText;
    String tryedit;
    private EditText mEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);


        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PREF_FRAG()).commit();

        //EditText f1config = (EditText)findViewById(R.id.edittext_preference1);
        //EditText f2config = (EditText)findViewById(R.id.edittext_preference2);

        //Log.d("yo",f1configs);
        //


        /*String f1configs = f1config.getText().toString();
        String f2configs= f2config.getText().toString();
        Intent i = getIntent();
        i.putExtra("f1config", f1configs);
        i.putExtra("f2config",f2configs);*/
        //EditText F1 = (EditText)findViewById(R.id.edittext_preference1);
    }

    public void onBackPressed()
    {
        String result;
        Intent data = getIntent();
        //data.putExtra("result",result);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String f1configs= prefs.getString("edittext_preference1","");
        String f2configs = prefs.getString("edittext_preference2","");
        //Intent i = getIntent();
        Log.d("yo", f1configs); //log for debugging purposes.
        data.putExtra("edittext_preference1", f1configs);
        data.putExtra("edittext_preference2", f2configs);
        setResult(SetPreferenceActivity.RESULT_OK,data);

        super.onBackPressed();
        finish();
    }


}
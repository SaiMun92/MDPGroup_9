package com.example.android.mdpandroid;


import android.app.Fragment;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class PREF_FRAG extends PreferenceFragment {

    public PREF_FRAG() {

    }

    /*public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);
        LayoutInflater inflater = LayoutInflater.from(getContext());


    };*/
    public void onCreate(Bundle savedInstanceState) {

        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.preferences);

        EditTextPreference edp_password = (EditTextPreference)findPreference("pref_key_account_password");

       EditTextPreference f1config = (EditTextPreference)findPreference("edittext_preference1");
        /*edp_password.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                Log.v(TAG, "Password is: " + (EditText) newValue.getText());
                return true;
            }
        });*/

    }


}


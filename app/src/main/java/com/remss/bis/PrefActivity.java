package com.remss.bis;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import java.util.Arrays;

public class PrefActivity extends PreferenceActivity
{

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment())
                .commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        private  SharedPreferences sharedPreferences;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref);
        }

        @Override
        public void onResume() {
            super.onResume();

            sharedPreferences = getPreferenceManager().getSharedPreferences();
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);

            PreferenceScreen preferenceScreen = getPreferenceScreen();
            for(int i = 0; i < preferenceScreen.getPreferenceCount(); i++)
            {
                setSummary(getPreferenceScreen().getPreference(i));
            }
        }

        @Override
        public void onPause()
        {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            android.preference.Preference pref = getPreferenceScreen().findPreference(key);
            setSummary(pref);
        }

        private  void setSummary(Preference pref)
        {
            if(pref instanceof EditTextPreference)
            {
                updateSummary((EditTextPreference) pref);
            }
            else if(pref instanceof ListPreference)
            {
                updateSummary((ListPreference) pref);
            }
            else if(pref instanceof MultiSelectListPreference)
            {
                updateSummary((MultiSelectListPreference) pref);
            }
        }

        private void updateSummary(MultiSelectListPreference pref)
        {
            pref.setSummary(Arrays.toString(pref.getValues().toArray()));
        }
        private void updateSummary(ListPreference pref)
        {
            pref.setSummary(pref.getValue());
        }
        private void updateSummary(EditTextPreference preference)
        {
            preference.setSummary(preference.getText());
        }
    }
}
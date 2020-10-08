package me.drblau.fumailapp.ui.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import me.drblau.fumailapp.MainActivity;
import me.drblau.fumailapp.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        Preference preference = preferenceScreen.findPreference("logoutButton");
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginData", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        assert preference != null;
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Erase Login Data and restart app
                //TODO: Remove app starting two times
                editor.clear();
                editor.commit();

                Intent startActivity = new Intent(getContext(), MainActivity.class);
                int mPendingIntentId = 123456;

                PendingIntent mPendingIntent = PendingIntent.getActivity(getContext(), mPendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                assert mgr != null;
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);

                return true;
            }
        });


        CheckBoxPreference stayLogged = preferenceScreen.findPreference("keepLoggedIn");
        stayLogged.setChecked(sharedPreferences.getBoolean("Keep_Login", false));
        stayLogged.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                editor.putBoolean("Keep_Login", (Boolean) newValue);
                editor.commit();
                return true;
            }
        });

        EditTextPreference signature = preferenceScreen.findPreference("signature");
        signature.setText(sharedPreferences.getString("signature", getString(R.string.default_signature)));
        signature.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                editor.putString("signature", newValue.toString().replace("\n", "<br>"));
                editor.commit();
                return false;
            }
        });
    }
}

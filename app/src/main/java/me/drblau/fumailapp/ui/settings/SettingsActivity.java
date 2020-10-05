package me.drblau.fumailapp.ui.settings;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import me.drblau.fumailapp.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        setTitle(R.string.action_settings);
        ActionBar actionBar = getSupportActionBar();

        //Enable getting out of intent
        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Bit of a hack, since I don't know the Button Id
        //But since there is only one Button in the ActionBar, we can ignore everything
        onBackPressed();

        return super.onOptionsItemSelected(item);
    }
}

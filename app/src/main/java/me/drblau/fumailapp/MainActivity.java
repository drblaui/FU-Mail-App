/**
 * This implements the ZeDat Unix Webmail as an App
 *
 * @author Alexander Rudolph
 * @version ALPHA 0.8.0
 * @since 2020-09-24
 */

package me.drblau.fumailapp;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import me.drblau.fumailapp.ui.create.MailCreatorActivity;
import me.drblau.fumailapp.ui.login.LoginDialogFragment;
import me.drblau.fumailapp.ui.settings.SettingsActivity;
import me.drblau.fumailapp.util.common.Settings;
import me.drblau.fumailapp.util.notifications.NotificationWorker;
import me.drblau.fumailapp.util.passwort_handling.Encrypt;

//CAREFUL, VERY XXX(problematic or misguiding code) HERE DUE TO SPAGHETTI FALLING OUT OF MY POCKET
public class MainActivity extends AppCompatActivity implements LoginDialogFragment.LoginDialogListener {
    //Helpers
    private AppBarConfiguration mAppBarConfiguration;
    private Encrypt encryptor;

    //Login
    private View view;
    private static final String ALIAS = "drblau.Keystore";
    private boolean isLoggedIn = false;


    private Settings settings;

    public void login(View view) {
        //Overlay Login Dialog over current view
        this.view = view;
        DialogFragment dialog = new LoginDialogFragment();
        dialog.show(getSupportFragmentManager(), "LoginDialogFragment");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(this);
        encryptor = new Encrypt();

        //If User saved his Login Data
        if(settings.getMail() != null) {
            isLoggedIn = true;
        }

        //Handle what to show
        AppBarConfiguration.Builder mAppBarConfigurationBuilder;
        if(isLoggedIn) {
            checkDeepLink();
            setContentView(R.layout.activity_main);

            Data inputData = new Data.Builder()
                    .putString("mail", settings.getMail())
                    .putString("password", settings.getPassword())
                    .putByteArray("iv", settings.getIv())
                    .build();
            PeriodicWorkRequest fetchRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                    .setInputData(inputData)
                    .build();

            WorkManager
                    .getInstance(this)
                    .enqueueUniquePeriodicWork(
                            "FetchUserMail",
                            ExistingPeriodicWorkPolicy.KEEP,
                            fetchRequest
                    );
            //Allow writing mails
            FloatingActionButton fab = findViewById(R.id.write_mail);
            fab.setOnClickListener(view -> {
                Intent write = new Intent(getApplicationContext(), MailCreatorActivity.class);
                startActivity(write);
            });

            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            mAppBarConfigurationBuilder = new AppBarConfiguration.Builder(R.id.nav_unread, R.id.nav_inbox, R.id.nav_spam, R.id.nav_trash, R.id.nav_sent, R.id.nav_drafts);

            //This may warn about duplicates, but it's actually supposed to be like that
            NavigationView navView = findViewById(R.id.nav_view);
            View header = navView.getHeaderView(0);

            //Change Header Text accordingly
            String username = settings.getUsername();
            String email = settings.getMail();
            TextView navUsername = header.findViewById(R.id.name);
            navUsername.setText(username);
            TextView eMail =  header.findViewById(R.id.email);
            eMail.setText(email);
        }
        else {
            setContentView(R.layout.activity_main_empty);
            // No need for passing IDs, but just pass empty to be sure
            mAppBarConfigurationBuilder = new AppBarConfiguration.Builder(R.id.nav_empty);

        }
        //Execution we need for both states
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        @SuppressLint("CutPasteId") NavigationView navigationView = findViewById(R.id.nav_view);
        //Make drawer drawable
        mAppBarConfigurationBuilder.setOpenableLayout(drawer);
        mAppBarConfiguration = mAppBarConfigurationBuilder.build();

        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Settings is a clickable ConstraintLayout, so it can float on the bottom
        View navSettings = findViewById(R.id.nav_settings);
        navSettings.setOnClickListener(v -> {
            //Show settings
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        });
    }

    private boolean serviceRunning(Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service Status", "Running");
                return true;
            }
        }
        Log.i("Service Status", "Not Running");
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Dialog dial = dialog.getDialog();

        assert dial != null;

        //Find Inputs and make them correct
        final EditText mail = dial.findViewById(R.id.email_input);
        final EditText pass = dial.findViewById(R.id.password_input);
        final EditText nam = dial.findViewById(R.id.name_input);
        final CheckBox check = dial.findViewById(R.id.keep_logged_in);
        String email = mail.getText().toString();
        //Make sure it's an ZeDat Mail (Note fu-berlin.de does not work)
        if(!email.contains("@zedat.fu-berlin.de")) {
            Snackbar.make(this.view, R.string.invalid_email, Snackbar.LENGTH_LONG).show();
            return;
        }
        String password = pass.getText().toString();
        String name = nam.getText().toString();
        String encPass = encrypt(password);
        String ivString = Base64.encodeToString(encryptor.getIv(), Base64.DEFAULT);
        //TODO: Well, this
        boolean keepLogin = check.isChecked();

        settings.update(Settings.PREFS_MAIL, email);
        settings.update(Settings.PREFS_USERNAME, name);
        settings.update(Settings.PREFS_PASSWORD, encPass);
        settings.update(Settings.PREFS_IV, ivString);
        settings.update(Settings.PREFS_STAY_LOGGED_IN, keepLogin);

        //Restart Application, so it knows we are logged in
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //Snackbar when User Cancels
        Objects.requireNonNull(dialog.getDialog()).cancel();
        Snackbar.make(this.view, R.string.login_needed, Snackbar.LENGTH_LONG).show();
    }

    /**
     *
     * @link https://gist.github.com/JosiasSena/3bf4ca59777f7dedcaf41a495d96d984
     */

    public String encrypt(String text) {
        try {
            final byte[] encryptedText = encryptor.encryptText(ALIAS, text);
            return Base64.encodeToString(encryptedText, Base64.DEFAULT);
        }
        catch (UnrecoverableEntryException | NoSuchAlgorithmException | NoSuchProviderException |
                KeyStoreException | IOException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | SignatureException |
                IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();

        }
        return "";
    }

    private void checkDeepLink(){
        //Check if user opened a mailto: link, so we can redirect to the MailCreatorActivity
        if (getIntent() != null && getIntent().getData() != null) {
            Uri data = getIntent().getData();
            String scheme = data.getScheme();
            //Be sure we only handle mailto
            assert scheme != null;
            if(scheme.equals("mailto")) {
                MailTo mail = MailTo.parse(data.toString());
                String receiver = mail.getTo();
                String subject = mail.getSubject();
                String body = mail.getBody();
                Log.d("FU-Mail-App","Created App instance over Scheme: " + scheme);
                Intent intent = new Intent(this,MailCreatorActivity.class);
                //Put into intent what we had in the mailto link
                if(receiver != null) {
                    intent.putExtra("receiver", receiver);
                }
                if(subject != null) {
                    intent.putExtra("subject", subject);
                }
                if(body != null) {
                    intent.putExtra("body", body);
                }
                startActivity(intent);
            }

        }
    }

    @Override
    protected void onDestroy() {
        /*
         * Files are attempted to be deleted at 3 different points.
         * Whenever the User closes the app completely (cleanup reasons)
         * When the User closes the MailCreator (TODO: Workaround for draft)
         * When the User has send the message (obvious reasons)
         * */
        File[] files = getFilesDir().listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        //Clear Preferences if User does not wish to be logged in
        //TODO: Does not (always) work
        if (!settings.getLoggedIn()) {
            settings.delete();
        }
        /*Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(getBaseContext(), Restarter.class);
        this.sendBroadcast(broadcastIntent);*/
        super.onDestroy();
        //startService(new Intent(this, NotificationService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        //startService(new Intent(getBaseContext(), NotificationService.class));
    }
}

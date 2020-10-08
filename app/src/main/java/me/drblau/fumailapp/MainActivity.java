/**
 * This implements the ZeDat Unix Webmail as an App
 *
 * @author Alexander Rudolph
 * @version INDEV 0.6.0
 * @since 2020-09-24
 */

package me.drblau.fumailapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.security.*;
import java.security.cert.*;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import me.drblau.fumailapp.ui.create.MailCreatorActivity;
import me.drblau.fumailapp.ui.login.LoginDialogFragment;
import me.drblau.fumailapp.ui.settings.SettingsActivity;
import me.drblau.fumailapp.util.mail.MailHandlerSMTP;
import me.drblau.fumailapp.util.passwort_handling.Decrypt;
import me.drblau.fumailapp.util.passwort_handling.Encrypt;

//CAREFUL, VERY XXX(problematic or misguiding code) HERE DUE TO SPAGHETTI FALLING OUT OF MY POCKET
public class MainActivity extends AppCompatActivity implements LoginDialogFragment.LoginDialogListener {
    //Helpers
    private AppBarConfiguration mAppBarConfiguration;
    private Encrypt encryptor;
    private Decrypt decryptor;
    private MailHandlerSMTP handler;
    private byte[] iv;

    //Login
    private View view;
    private static final String ALIAS = "drblau.Keystore";
    private boolean isLoggedIn = false;

    //SharedPreferences
    private static final String PREFS_NAME = "loginData";
    private static final String PREFS_MAIL = "Email";
    private static final String mailDefault = "john.doe@fu-berlin.de";
    private static final String PREFS_PASSWORD = "Password";
    private static final String passDefault = "password";
    private static final String PREFS_USERNAME = "Username";
    private static final String usernameDefault = "FU Berlin Mail App User";
    private static final String PREFS_STAY_LOGGED_IN = "Keep_Login";
    private static final String PREFS_IV = "IV";

    private SharedPreferences settings;

    public void login(View view) {
        //Overlay Login Dialog over current view
        this.view = view;
        DialogFragment dialog = new LoginDialogFragment();
        dialog.show(getSupportFragmentManager(), "LoginDialogFragment");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        encryptor = new Encrypt();
        //Decrypt could have problems on init (it obviously shouldn't)
        try {
            decryptor = new Decrypt();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException |
                IOException e) {
            e.printStackTrace();
        }

        //If User saved his Login Data
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if(settings.getString(PREFS_MAIL, null) != null) {
            isLoggedIn = true;
        }
        if(settings.getString(PREFS_IV, null) != null) {
            //Get Encryption IV
            iv = Base64.decode(settings.getString(PREFS_IV, null), Base64.DEFAULT);
        }

        //Handle what to show
        AppBarConfiguration.Builder mAppBarConfigurationBuilder;
        if(isLoggedIn) {
            checkDeepLink();
            setContentView(R.layout.activity_main);

            //Allow writing mails
            FloatingActionButton fab = findViewById(R.id.write_mail);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent write = new Intent(getApplicationContext(), MailCreatorActivity.class);
                    startActivity(write);
                }
            });

            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            mAppBarConfigurationBuilder = new AppBarConfiguration.Builder(R.id.nav_unread, R.id.nav_inbox, R.id.nav_spam, R.id.nav_trash, R.id.nav_sent, R.id.nav_drafts);

            //This may warn about duplicates, but it's actually supposed to be like that
            NavigationView navView = findViewById(R.id.nav_view);
            View header = navView.getHeaderView(0);

            //Change Header Text accordingly
            String username = settings.getString(PREFS_USERNAME, usernameDefault);
            String email = settings.getString(PREFS_MAIL, mailDefault);
            TextView navUsername = (TextView) header.findViewById(R.id.name);
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
        NavigationView navigationView = findViewById(R.id.nav_view);
        //May be deprecated, but still works
        //Make drawer drawable
        mAppBarConfigurationBuilder.setDrawerLayout(drawer);
        mAppBarConfiguration = mAppBarConfigurationBuilder.build();

        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Settings is a clickable ConstraintLayout, so it can float on the bottom
        View navSettings = findViewById(R.id.nav_settings);
        navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show settings
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

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
        final Spinner end = dial.findViewById(R.id.email_ending);
        final CheckBox check = dial.findViewById(R.id.keep_logged_in);
        String email = mail.getText().toString() + end.getSelectedItem().toString();
        String password = pass.getText().toString();
        String name = nam.getText().toString();
        String encPass = encrypt(password);
        String ivString = Base64.encodeToString(encryptor.getIv(), Base64.DEFAULT);
        //TODO: Well, this
        boolean keepLogin = check.isChecked();

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_MAIL, email);
        editor.putString(PREFS_PASSWORD, encPass);
        editor.putString(PREFS_USERNAME, name);
        editor.putBoolean(PREFS_STAY_LOGGED_IN, keepLogin);
        editor.putString(PREFS_IV, ivString);
        editor.apply();

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
     * For Encrypt and Decrypt look at:
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
    /**
     * For Encrypt and Decrypt look at:
     * @link https://gist.github.com/JosiasSena/3bf4ca59777f7dedcaf41a495d96d984
     */
    public String decrypt(String text) {

        try {
            assert decryptor != null;
            byte[] txt = Base64.decode(text, Base64.DEFAULT);
            return decryptor.decryptData(ALIAS, txt, iv);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void checkDeepLink(){
        if (getIntent() != null && getIntent().getData() != null) {
            Uri data = getIntent().getData();
            String scheme = data.getScheme();
            MailTo mail = MailTo.parse(data.toString());
            String receiver = mail.getTo();
            String subject = mail.getSubject();
            String body = mail.getBody();
            Log.d("FU-Mail-App","Created App instance over Scheme: " + scheme);
            Intent intent = new Intent(this,MailCreatorActivity.class);
            intent.putExtra("receiver", receiver);
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

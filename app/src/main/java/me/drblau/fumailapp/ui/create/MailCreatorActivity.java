package me.drblau.fumailapp.ui.create;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Patterns;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import me.drblau.fumailapp.R;
import me.drblau.fumailapp.util.mail.MailHandlerSMTP;
import me.drblau.fumailapp.util.passwort_handling.Decrypt;


//TODO: Comment
public class MailCreatorActivity extends AppCompatActivity implements ChangeMailDialogFragment.ChangeMailDialogListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 6669995;
    Button mail_sender;
    Button attachment_adder;
    EditText mail_to;
    private MailHandlerSMTP handler;
    private SharedPreferences settings;
    private Decrypt decryptor;
    String test = null;

    private View currEdit;

    private static final String ALIAS = "drblau.Keystore";
    private byte[] iv;
    private boolean hasAttachment = false;
    private ArrayList<String> receivers = new ArrayList<>();


    //SharedPreferences
    private static final String PREFS_NAME = "loginData";
    private static final String PREFS_MAIL = "Email";
    private static final String mailDefault = "john.doe@fu-berlin.de";
    private static final String PREFS_PASSWORD = "Password";
    private static final String passDefault = "password";
    private static final String PREFS_USERNAME = "Username";
    private static final String usernameDefault = "FU Berlin Mail App User";
    private static final String PREFS_IV = "IV";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_mail);
        mail_sender = findViewById(R.id.mail_send);
        attachment_adder = findViewById(R.id.button_attachments);

        //Decrypt could have problems on init (it obviously shouldn't)
        try {
            decryptor = new Decrypt();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException |
                IOException e) {
            e.printStackTrace();
        }
        settings = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if(settings.getString(PREFS_IV, null) != null) {
            //Get Encryption IV
            iv = Base64.decode(settings.getString(PREFS_IV, null), Base64.DEFAULT);
        }
        handler = new MailHandlerSMTP(settings.getString(PREFS_MAIL, mailDefault), decrypt(settings.getString(PREFS_PASSWORD, passDefault)), settings.getString(PREFS_USERNAME, usernameDefault));

        setTitle(R.string.mail_header);
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Get data from opened mailto Link
        if(getIntent() != null) {
            String receivers = getIntent().getStringExtra("receiver");
            String subject = getIntent().getStringExtra("subject");
            String body = getIntent().getStringExtra("body");

            if(receivers != null) {
                ((EditText) findViewById(R.id.mail_to)).setText(receivers);
            }


            if(subject != null) {
                ((EditText) findViewById(R.id.mail_subject)).setText(subject);
            }
            if(body != null) {
                ((EditText) findViewById(R.id.mail_content)).setText(body);
            }
        }
        mail_to = findViewById(R.id.mail_to);
        mail_to.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0 && s.toString().charAt(s.toString().length() - 1) == ' ') {
                    validateEmail();
                }
            }
        });
        mail_to.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    validateEmail();
                }
            }
        });

        attachment_adder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(intent, 1);
            }
        });

        mail_sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Implement attachments https://www.tutorialspoint.com/javamail_api/javamail_api_send_email_with_attachment.htm
                //https://www.dev2qa.com/how-to-get-real-file-path-from-android-uri/
                String subject = ((EditText) findViewById(R.id.mail_subject)).getText().toString();
                String message = ((EditText) findViewById(R.id.mail_content)).getText().toString();
                if(receivers.isEmpty()) {
                    mail_to.setError(getString(R.string.error_receiver));
                    return;
                }
                if(subject.isEmpty()) {
                    ((EditText) findViewById(R.id.mail_subject)).setError(getString(R.string.error_subject));
                    return;
                }
                if(message.isEmpty() && !hasAttachment) {
                    ((EditText) findViewById(R.id.mail_content)).setError(getString(R.string.error_body));
                    return;
                }

                boolean success;
                if(test != null) {
                    success = handler.sendMessage(receivers, subject, message, test);
                }
                else {
                    success = handler.sendMessage(receivers, subject, message, settings.getString("signature", getString(R.string.default_signature)));
                }

                if(success) {
                    Toast.makeText(getApplicationContext(), R.string.mail_send_success, Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.mail_send_failed, Toast.LENGTH_LONG).show();
                }

                onBackPressed();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
             test = data.getData().getPath();

        }
    }

    private void validateEmail() {
        EditText mailEdit = findViewById(R.id.mail_to);
        String mail = mailEdit.getText().toString();
        if(isValidMail(mail)) {
            mailEdit.setText("");
            LinearLayout layout = findViewById(R.id.receivers);
            layout.setPadding(0, 10, 0,0);
            TextView tw = findViewById(R.id.scroll_receiver);
            tw.append(":");
            tw.setVisibility(View.VISIBLE);
            tw = new TextView(this);
            tw.setText(mail.trim());
            tw.setBackground(getDrawable(R.drawable.round_corner));
            tw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person, 0, 0, 0);
            tw.setGravity(Gravity.CENTER);
            tw.setPadding(8,3,8,3);
            tw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currEdit = v;
                    DialogFragment dialog = new ChangeMailDialogFragment();
                    Bundle args = new Bundle();
                    args.putString("mail", ((TextView)v).getText().toString());
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), "ChangeMailDialogFragment");
                }
            });
            layout.addView(tw);
            receivers.add(mail.trim());
        }
        else {
            //Show error
            mailEdit.setError(getString(R.string.mail_invalid));
        }
    }

    private boolean isValidMail(String mail) {
        if(mail.isEmpty()) {
            return false;
        }
        else return Patterns.EMAIL_ADDRESS.matcher(mail.trim()).matches();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //As hacky as Settings ¯\_(ツ)_/¯
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

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

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Dialog dial = dialog.getDialog();
        ((TextView) currEdit).setText(((EditText) dial.findViewById(R.id.mail_changer)).getText().toString());
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.getDialog().cancel();
    }
}

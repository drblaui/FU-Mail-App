package me.drblau.fumailapp.ui.create;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import me.drblau.fumailapp.R;
import me.drblau.fumailapp.util.mail.MailHandlerSMTP;
import me.drblau.fumailapp.util.passwort_handling.Decrypt;


//TODO: Allow user to remove receiver and attachments
public class MailCreatorActivity extends AppCompatActivity implements ChangeMailDialogFragment.ChangeMailDialogListener, ChangeFileNameDialogFragment.ChangeFileNameDialogListener {
    //TODO: Cleanup
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 6669995;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 6669996;
    Button mail_sender;
    Button attachment_adder;
    EditText mail_to;
    private MailHandlerSMTP handler;
    private SharedPreferences settings;
    private Decrypt decryptor;

    private View currEdit;

    private static final String ALIAS = "drblau.Keystore";
    private byte[] iv;
    private boolean hasAttachment = false;
    private ArrayList<String> receivers = new ArrayList<>();
    /*
     * Attachments are saved as <Filename, Filepath> so we don't have to delete and recreate the file
     * every time a user changes the name. Filename setting is handled in MailHandler anyways
     */
    private HashMap<String, String>  attachments = new HashMap<>();


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
                //Check for valid mail after every space
                if(s.length() > 0 && s.toString().charAt(s.toString().length() - 1) == ' ') {
                    validateEmail();
                }
            }
        });
        // Also check for valid mail after unfocus of edittext
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
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    //TODO Open intent AFTER user accepted or throw error
                }
                else {
                    //Let user pick any File
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 1);
                }
            }
        });

        mail_sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get message contents
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

                //TODO: Error handling let's some errors pass
                boolean success;
                //If any attachments are attachable
                if(!attachments.isEmpty()) {
                    success = handler.sendMessage(receivers, subject, message, attachments, settings.getString("signature", getString(R.string.default_signature)), getFilesDir());
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

            try {
                //NEVER EVER TOUCH THIS CODE AGAIN, THIS TOOK DAYS TO GET IT WORKING
                Uri uri = data.getData();
                System.out.println(uri.toString());
                InputStream is = getContentResolver().openInputStream(uri);
                String filename = uri.getLastPathSegment().substring(uri.getLastPathSegment().lastIndexOf("/") + 1);
                File file = new File(getFilesDir().getAbsolutePath() + "/" + filename);
                FileOutputStream os = new FileOutputStream(file);
                /*
                * Write from external storage to App Storage, so the app has always permission to
                * write on the file
                */
                int read;
                byte[] bytes = new byte[1024];
                while((read = is.read(bytes)) != -1) {
                    os.write(bytes, 0, read);
                }
                os.close();
                is.close();
                System.out.println(Arrays.toString(getFilesDir().listFiles()));
                attachments.put(filename, file.getPath());
                //You can touch now again
                //Append File for user to see
                String name = file.getName();
                LinearLayout layout = findViewById(R.id.attachments);
                layout.setPadding(0, 10, 0, 0);
                TextView tw = findViewById(R.id.scroll_attachment);
                tw.append(":");
                tw.setVisibility(View.VISIBLE);
                tw = new TextView(this);
                tw.setText(name);
                tw.setBackground(getDrawable(R.drawable.round_corner));
                tw.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_file, 0, 0, 0);
                tw.setGravity(Gravity.CENTER);
                tw.setPadding(8,3,8,3);
                tw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Allow edit of filename
                        currEdit = v;
                        DialogFragment dialog = new ChangeFileNameDialogFragment();
                        Bundle args = new Bundle();
                        args.putString("filename", ((TextView) v).getText().toString());
                        dialog.setArguments(args);
                        dialog.show(getSupportFragmentManager(), "ChangeFilenameDialogFragment");
                    }
                });
                layout.addView(tw);
                hasAttachment = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static String getPath(Context context, Uri uri) {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private void validateEmail() {
        EditText mailEdit = findViewById(R.id.mail_to);
        String mail = mailEdit.getText().toString();
        if(isValidMail(mail)) {
            //Append to receivers list (visually and internal)
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
                    //Allow mail edit
                    currEdit = v;
                    DialogFragment dialog = new ChangeMailDialogFragment();
                    Bundle args = new Bundle();
                    args.putString("mail", ((TextView)v).getText().toString());
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), "ChangeMailDialogFragment");
                }
            });
            layout.addView(tw);
            //Make sure we don't have the space (because we validate AFTER space is pressed)
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
        //Mail REGEX
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
    //TODO: Maybe change to onMailDialogPositiveClick to avoid confusion
    public void onDialogPositiveClick(DialogFragment dialog) {
        //Positive Dialog Option for Mail Change
        Dialog dial = dialog.getDialog();
        //Get Mails
        String oldName = ((TextView) dial.findViewById(R.id.mail_old)).getText().toString();
        String newName = ((EditText) dial.findViewById(R.id.mail_changer)).getText().toString();
        ((TextView) currEdit).setText(newName);
        //Replace Mail in receiver ArrayList
        for(int i = 0; i < receivers.size(); i++) {
            if(receivers.get(i).equals(oldName)) {
                receivers.set(i, newName);
                break;
            }
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //Globally the same for all Dialogs
        dialog.getDialog().cancel();
    }

    @Override
    public void onFilenameDialogPositiveClick(DialogFragment dialog) {
        Dialog dial = dialog.getDialog();
        //Get names
        String newName = ((EditText) dial.findViewById(R.id.filename_changer)).getText().toString() + ((TextView) dial.findViewById(R.id.filetype)).getText();
        String oldName = ((TextView) dial.findViewById(R.id.filename_old)).getText().toString();
        ((TextView) currEdit).setText(newName);

        //Get path of old file name, delete key and recreate with new name
        String path = attachments.remove(oldName);
        attachments.put(newName, path);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File[] files = getFilesDir().listFiles();
        if(files != null) {
            for(File file: files) {
                file.delete();
            }
        }
    }
}

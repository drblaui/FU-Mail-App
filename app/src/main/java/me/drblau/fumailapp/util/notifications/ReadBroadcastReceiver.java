package me.drblau.fumailapp.util.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import me.drblau.fumailapp.util.mail.MailMarkRead;
import me.drblau.fumailapp.util.passwort_handling.Decrypt;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ReadBroadcastReceiver extends BroadcastReceiver {
    private static final String ALIAS = "drblau.Keystore";
    @Override
    public void onReceive(Context context, Intent intent) {
        String folder = intent.getStringExtra("folder");
        int id = intent.getIntExtra("mailId", 0);
        String mail = intent.getStringExtra("mail");
        String password = intent.getStringExtra("pass");
        int notificationId = intent.getIntExtra("notificId", 0);
        byte[] iv = intent.getByteArrayExtra("iv");

        try {
            MailMarkRead marker = new MailMarkRead(mail, decrypt(password, iv), folder, id);
            marker.execute();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            e.printStackTrace();
        }
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        assert mNotificationManager != null;
        mNotificationManager.cancel(notificationId);
    }

    private String decrypt(String text, byte[] iv) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        Decrypt decryptor = new Decrypt();

        try {
            byte[] txt = Base64.decode(text, Base64.DEFAULT);
            return decryptor.decryptData(ALIAS, txt, iv);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}

package me.drblau.fumailapp.util.mail;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

import com.sun.mail.imap.IMAPMessage;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import me.drblau.fumailapp.util.passwort_handling.Decrypt;


//Unfinished
public class MailHandlerIMAP {
    private final String pass;
    private final String mail;
    private Session session;
    private Store store;

    private final String HOST = "mail.zedat.fu-berlin.de";
    private final String PORT = "143";
    private static final String ALIAS = "drblau.Keystore";
    private final byte[] iv;

    public MailHandlerIMAP(String mail, String password, byte[] iv) {
        this.mail = mail;
        this.pass = password;
        this.iv = iv;
        //login();
    }

    private void login() {
        Properties properties = new Properties();
        session = Session.getDefaultInstance(properties, null);
        try {
            store = session.getStore("imap");
            store.connect(HOST, mail, pass);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public MessageSkeleton[] fetch(String folder) {
        try {
            return new MailFetcher(mail, decrypt(pass), folder).execute().get();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;

    }

    public MessageSkeleton fetch(String folder, int id) {
        try {
            return new SpecificMailFetcher(mail, decrypt(pass), folder, id).execute().get();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String decrypt(String text) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
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

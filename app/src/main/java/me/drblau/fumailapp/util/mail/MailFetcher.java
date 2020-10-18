package me.drblau.fumailapp.util.mail;

import android.os.AsyncTask;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

//Unfinished
public class MailFetcher extends AsyncTask<Void, Void, Void> {
    private final String mail;
    private final String pass;
    private final String HOST = "mail.zedat.fu-berlin.de";
    private final String folder;

    public MailFetcher(String mail, String pass, String folder) {
        this.mail = mail;
        this.pass = pass;
        this.folder = folder;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(properties, null);
            Store store = session.getStore();
            store.connect(HOST, mail, pass);
            Folder inbox = store.getFolder(folder);
            inbox.open(Folder.READ_ONLY);
            Message msg = inbox.getMessage(inbox.getMessageCount());
            Address[] in = msg.getFrom();
            for(Address address : in) {
                System.out.println(address);
            }
            Multipart mp = (Multipart) msg.getContent();
            BodyPart bp = mp.getBodyPart(0);
            System.out.println("SENT DATE: " + msg.getSentDate());
            System.out.println("SUBJECT: " + msg.getSubject());
            System.out.println("CONTENT: " + bp.getContent());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

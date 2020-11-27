package me.drblau.fumailapp.util.mail;

import android.os.AsyncTask;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

//Unfinished
public class MailMarkRead extends AsyncTask<Void, Void, Void> {
    private final String mail;
    private final String pass;
    private final String HOST = "mail.zedat.fu-berlin.de";
    private String folder;
    private int id;

    public MailMarkRead(String mail, String pass, String folder, int id) {
        this.mail = mail;
        this.pass = pass;
        this.folder = folder;
        this.id = id;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
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
            inbox.open(Folder.READ_WRITE);
            Message message = inbox.getMessage(id);
            message.setFlag(Flags.Flag.SEEN, true);
        }
        catch (MessagingException e){
            e.printStackTrace();
        }
        return null;
    }
}

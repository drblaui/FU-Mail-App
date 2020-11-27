package me.drblau.fumailapp.util.mail;

import android.os.AsyncTask;
import android.os.Handler;

import java.util.Arrays;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

//Unfinished
public class SpecificMailFetcher extends AsyncTask<Void, Void, MessageSkeleton> {
    private final String mail;
    private final String pass;
    private final String HOST = "mail.zedat.fu-berlin.de";
    private String folder;
    private int id;
    MessageSkeleton message;

    public SpecificMailFetcher(String mail, String pass, String folder, int id) {
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
    protected MessageSkeleton doInBackground(Void... voids) {
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(properties, null);
            Store store = session.getStore();
            store.connect(HOST, mail, pass);
            Folder inbox = store.getFolder(folder);
            inbox.open(Folder.READ_WRITE);
            Message message = inbox.getMessage(this.id);
            message.setFlag(Flags.Flag.SEEN, true);
            this.message = new MessageSkeleton(message);


            return this.message;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(MessageSkeleton messageSkeleton) {
        super.onPostExecute(messageSkeleton);
    }
}

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
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;

//Unfinished
public class MailFetcher extends AsyncTask<Void, Void, MessageSkeleton[]> {
    private final String mail;
    private final String pass;
    private final String HOST = "mail.zedat.fu-berlin.de";
    private String folder;
    private boolean unseen = false;
    private boolean unnotified = false;
    MessageSkeleton[] messages;

    public MailFetcher(String mail, String pass, String folder) {
        this.mail = mail;
        this.pass = pass;
        this.folder = folder;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected MessageSkeleton[] doInBackground(Void... voids) {
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(properties, null);
            Store store = session.getStore();
            store.connect(HOST, mail, pass);
            if(folder.equals("Unread")) {
                unseen = true;
                folder = "INBOX";
            }
            else if(folder.equals("Unread|Notify")) {
                unseen = true;
                folder = "INBOX";
                unnotified = true;

            }
            Folder inbox = store.getFolder(folder);
            inbox.open(Folder.READ_ONLY);
            Message[] messages;
            Flags notifiedFlag = new Flags("notified");
            if(unseen && unnotified) {
                Flags seen = new Flags(Flags.Flag.SEEN);
                FlagTerm unseen = new FlagTerm(seen, false);
                FlagTerm unnotified = new FlagTerm(notifiedFlag, false);
                SearchTerm search = new AndTerm(unseen, unnotified);
                messages = inbox.search(search);
            }
            else if (unseen) {
                messages = inbox.search(new FlagTerm(new Flags(
                        Flags.Flag.SEEN
                ),false));
            }
            else {
                messages = inbox.getMessages();
            }
            //For some reason we can't pass Message[]
            MessageSkeleton[] msgs = new MessageSkeleton[messages.length];
            int i = 0;
            for(Message message : messages) {
                msgs[i] = new MessageSkeleton(message);
                if(unnotified) {
                    message.setFlags(notifiedFlag, true);
                }
                i++;
            }
            this.messages = msgs;
            return this.messages;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(MessageSkeleton[] messageSkeletons) {
        super.onPostExecute(messageSkeletons);
    }
}

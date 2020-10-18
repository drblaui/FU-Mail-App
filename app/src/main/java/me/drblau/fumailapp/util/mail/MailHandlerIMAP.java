package me.drblau.fumailapp.util.mail;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;


//Unfinished
public class MailHandlerIMAP {
    private final String pass;
    private final String mail;
    private Session session;
    private Store store;

    private final String HOST = "mail.zedat.fu-berlin.de";
    private final String PORT = "143";

    public MailHandlerIMAP(String mail, String password) {
        this.mail = mail;
        this.pass = password;

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

    public Message[] fetch(String folder) throws MessagingException, ExecutionException, InterruptedException {
        //MailFetcher fetcher = new MailFetcher(mail, pass, folder);
        //Object test = fetcher.execute().get();

        //System.out.println(test);

        return null;

    }
}

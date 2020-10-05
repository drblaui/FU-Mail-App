package me.drblau.fumailapp.util.mail;

import android.os.AsyncTask;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

public class MailSender extends AsyncTask {
    private Message message;
    MailSender(Message message) {
        this.message = message;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        //Either this works sometimes or always, time will tell
        try {
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

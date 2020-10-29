package me.drblau.fumailapp.util.mail;

import android.os.AsyncTask;
import java.io.File;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

public class MailSender extends AsyncTask {
    private Message message;
    private File filesDir;
    MailSender(Message message, File filesDir) {
        this.message = message;
        this.filesDir = filesDir;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        //Either this works sometimes or always, time will tell
        try {
            Transport.send(message);
            if(filesDir != null) {
                File[] files = filesDir.listFiles();
                if(files != null) {
                    for(File file : files) {
                        file.delete();
                    }
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

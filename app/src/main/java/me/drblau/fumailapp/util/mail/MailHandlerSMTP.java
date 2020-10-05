package me.drblau.fumailapp.util.mail;


import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailHandlerSMTP {
    private final String email;
    private final String pass;
    private final String username;
    private final String HOST = "mail.zedat.fu-berlin.de";
    private final String PORT = "587";
    private Session session;

    public MailHandlerSMTP(String mail, String password, String username) {
        email = mail;
        pass = password;
        this.username = username;
        //Save everything and login
        login();
    }

    private void login() {
        //Generate Session
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", HOST);
        properties.put("mail.smtp.port", PORT);

        session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, pass);
            }
        });
    }

    public boolean sendMessage(String receiver, String text, String subject) {
        try {
            //Generate Message and give it to MailSender, since Sending should be Async
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email, username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
            message.setSubject(subject);
            message.setText(text);

           MailSender sm = new MailSender(message);
           sm.execute();

            return true;
        }
        catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

}

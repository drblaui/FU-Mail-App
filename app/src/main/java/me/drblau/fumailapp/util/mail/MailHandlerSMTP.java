package me.drblau.fumailapp.util.mail;


import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

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

    public boolean sendMessage(ArrayList<String> receivers, String subject, String text, String signature) {
        try {
            //Generate Message and give it to MailSender, since Sending should be Async
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email, username));
            Address[] to = new Address[receivers.size()];
            int counter = 0;

            for(String receiver : receivers) {
                to[counter] = new InternetAddress(receiver);
                counter++;
            }
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject);
            //Allows HTML Formatting

            text = text + "<br>" + signature;
            message.setContent(text, "text/html");

           MailSender sm = new MailSender(message);
           sm.execute();
            return true;
        }
        catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendMessage(ArrayList<String> receivers, String subject, String text, String filepath, String signature) {
        try {
            //Generate Message and give it to MailSender, since Sending should be Async
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email, username));
            Address[] to = new Address[receivers.size()];
            int counter = 0;

            for(String receiver : receivers) {
                to[counter] = new InternetAddress(receiver);
                counter++;
            }
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject);
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(text, "text/html");
            Multipart multipart = new MimeMultipart();

            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();

            DataSource source = new FileDataSource(filepath);
            messageBodyPart.setDataHandler(new DataHandler((source)));
            messageBodyPart.setFileName(filepath);
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

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

package me.drblau.fumailapp.util.mail;

import java.io.IOException;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

//Unfinished
public class MessageSkeleton {
    private Address[] from = null;
    private Date sentDate = null;
    private String subject = null;
    private Object content = null;
    private final String preview;

    public MessageSkeleton(Message msg) {
        try {
            this.from = msg.getFrom();
            this.sentDate = msg.getSentDate();
            this.subject = msg.getSubject();
            this.content = ((Multipart) msg.getContent()).getBodyPart(0).getContent();
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
        this.preview = makePreview();
    }

    private String makePreview() {
        return content.toString().substring(16) + "...";
    }

    public Address[] getFrom() {
        return from;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public Object getContent() {
        return content;
    }

    public String getPreview() {
        return preview;
    }

    public String getSubject() {
        return subject;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public void setFrom(Address[] from) {
        this.from = from;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}

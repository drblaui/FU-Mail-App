package me.drblau.fumailapp.util.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;

//Unfinished
public class MessageSkeleton {
    private Address[] from = null;
    private Date sentDate = null;
    private String subject = null;
    private String content = null;
    private String contentType = null;
    private String folder = null;
    private final String preview;
    private int id = -1;

    public MessageSkeleton(Message msg) {
        try {
            this.from = msg.getFrom();
            this.sentDate = msg.getSentDate();
            this.subject = msg.getSubject();
            this.content =  getText(msg);
            this.contentType = msg.getContentType();
            this.id = msg.getMessageNumber();
            this.folder = msg.getFolder().getName();
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
        this.preview = makePreview();
    }

    private String makePreview() {
        try {
            //Will trust Stackoverflow with this regex
            String body = content.replaceAll("<[^>]*>", "");
            if (body.length() > 156) {
                return body.substring(0, 155) + "...";
            }
            else {
                return body;
            }

        }
        catch (Exception e) {
            return "Error";
        }
    }

    public Address[] getFrom() {
        return from;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public String getContent() {
        return content;
    }

    public String getPreview() {
        return preview;
    }

    public String getSubject() {
        return subject;
    }

    public String getFromToString() {
        StringBuilder addresses = new StringBuilder();
        String containsNameRegex = ".*<*>";
        for(Address address : from) {
            if (address.toString().matches(containsNameRegex)) {
                addresses.append(address.toString().substring(0, address.toString().indexOf("<")));
            }
            else {
                addresses.append(address.toString());
            }
            addresses.append(", ");
        }
        return addresses.toString().substring(0, addresses.toString().length() - 2);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFrom(Address[] from) {
        this.from = from;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public int getId() {
        return id;
    }

    public String getFolder() {
        return folder;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }


    private String getText(Message message) throws IOException, MessagingException {
        String res = "";
        if(message.isMimeType("text/plain")) {
            res = message.getContent().toString();
        }
        else if(message.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) message.getContent();
            res = getTextFromMultipart(multipart);
        }
        return res;
    }

    private String getTextFromMultipart(MimeMultipart multipart) throws MessagingException, IOException {
        String res = "";
        int count = multipart.getCount();
        for(int i = 0; i < count; i++) {
            BodyPart part = multipart.getBodyPart(i);
            if(part.isMimeType("text/plain")) {
                res = res + "\n" + part.getContent();
                break;
            }
            else if(part.isMimeType("text/html")) {
                String html = (String) part.getContent();
                res = res + "\n" + org.jsoup.Jsoup.parse(html).text();
            }
            else if(part.getContent() instanceof MimeMultipart) {
                res = res + getTextFromMultipart((MimeMultipart) part.getContent());
            }
        }
        return res;
    }

}

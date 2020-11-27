package me.drblau.fumailapp.ui.inbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.sun.mail.imap.protocol.MessageSet;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Date;

import me.drblau.fumailapp.R;
import me.drblau.fumailapp.ui.MailDetail;
import me.drblau.fumailapp.util.mail.MailHandlerIMAP;
import me.drblau.fumailapp.util.mail.MessageSkeleton;
import me.drblau.fumailapp.util.passwort_handling.Decrypt;

public class InboxFragment extends Fragment {
    private byte[] iv;

    private static final String ALIAS = "drblau.Keystore";

    //SharedPreferences
    private static final String PREFS_NAME = "loginData";
    private static final String PREFS_MAIL = "Email";
    private static final String mailDefault = "john.doe@fu-berlin.de";
    private static final String PREFS_PASSWORD = "Password";
    private static final String PREFS_USERNAME = "Username";
    private static final String usernameDefault = "FU Berlin Mail App User";
    private static final String PREFS_STAY_LOGGED_IN = "Keep_Login";
    private static final String PREFS_IV = "IV";

    private SharedPreferences settings;


    @Nullable
    @Override
    //TODO: Make settings own class
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String email = settings.getString(PREFS_MAIL, mailDefault);
        String pass = settings.getString(PREFS_PASSWORD, "password");
        if(settings.getString(PREFS_IV, null) != null) {
            iv = Base64.decode(settings.getString(PREFS_IV, null), Base64.DEFAULT);
        }
        MailHandlerIMAP handler = new MailHandlerIMAP(email, pass, iv);
        MessageSkeleton[] messages = handler.fetch("INBOX");
        sort(messages, 0, messages.length - 1);
        View root = inflater.inflate(R.layout.fragment_inbox, container, false);
        final TextView tw = root.findViewById(R.id.text_inbox);
        if(messages.length == 0) {
            tw.setText(getText(R.string.inbox_fragment));
        }
        else {
            LinearLayout layout = root.findViewById(R.id.inbox_parent);
            for(MessageSkeleton message : messages) {
                View constraintLayout = inflater.inflate(R.layout.email_preview, null);
                TextView sender = constraintLayout.findViewById(R.id.mail_sender);
                if(message.getFromToString().length() > 30) {
                    sender.setText(message.getFromToString().substring(0, 25) + "...");
                }
                else {
                    sender.setText(message.getFromToString());
                }

                TextView sentDate = constraintLayout.findViewById(R.id.mail_sent_date);
                Date sent = message.getSentDate();
                Date today = new Date();
                //I will NEVER use Calendar
                if(sent.getDate() == today.getDate() && sent.getMonth() == today.getMonth() && sent.getYear() == today.getYear()) {
                    String hour = Integer.toString(sent.getHours());
                    if(sent.getHours() < 10) {
                        hour = "0" + hour;
                    }
                    String minute = Integer.toString(sent.getMinutes());
                    if(sent.getMinutes() < 10) {
                        minute = "0" + minute;
                    }
                    sentDate.setText(hour + ":" + minute);
                }
                else {
                    String day = Integer.toString(sent.getDate());
                    if(sent.getDate() < 10) {
                        day = "0" + day;
                    }
                    String month = Integer.toString(sent.getMonth());
                    if(sent.getMonth() < 10) {
                        month = "0" + month;
                    }
                    //getYear() returns the year - 1900 for whatever reason
                    sentDate.setText(day + "." + month + "." + (sent.getYear() + 1900));
                }
                TextView messagePreview = constraintLayout.findViewById(R.id.mail_preview);
                messagePreview.setText(message.getPreview());
                TextView subject = constraintLayout.findViewById(R.id.subject);
                subject.setText(message.getSubject());
                constraintLayout.setOnClickListener(v -> {
                    Intent detail = new Intent(getContext(), MailDetail.class);
                    detail.putExtra("mailId", message.getId());
                    detail.putExtra("folder", message.getFolder());
                    startActivity(detail);
                });
                layout.addView(constraintLayout);
            }
        }


        return root;
    }

    private MessageSkeleton[] reverse(MessageSkeleton[] toSort) {
        MessageSkeleton[] reversed = new MessageSkeleton[toSort.length];
        int i = toSort.length - 1;
        for(MessageSkeleton message : toSort) {
            reversed[i] = message;
            i--;
        }
        return reversed;
    }

    //Quicksort
    private void sort(MessageSkeleton[] toSort, int begin, int end) {
        if(begin < end) {
            int partitionIndex = partition(toSort, begin, end);
            sort(toSort, begin, partitionIndex - 1);
            sort(toSort, partitionIndex + 1, end);
        }


    }
    private int partition (MessageSkeleton[] arr, int begin, int end) {
        MessageSkeleton pivot = arr[end];
        int i = (begin-1);
        for(int j = begin; j < end; j++) {
            if(arr[j].getSentDate().compareTo(pivot.getSentDate()) > 0) {
                i++;

                MessageSkeleton swapTemp = arr[i];
                arr[i] = arr[j];
                arr[j] = swapTemp;
            }
        }

        MessageSkeleton swapTemp = arr[i+1];
        arr[i+1] = arr[end];
        arr[end] = swapTemp;

        return i+1;
    }
}

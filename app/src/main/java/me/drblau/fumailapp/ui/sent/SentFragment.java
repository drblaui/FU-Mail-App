package me.drblau.fumailapp.ui.sent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.Date;

import me.drblau.fumailapp.R;
import me.drblau.fumailapp.ui.MailDetail;
import me.drblau.fumailapp.ui.inbox.InboxViewModel;
import me.drblau.fumailapp.util.mail.MailHandlerIMAP;
import me.drblau.fumailapp.util.mail.MessageSkeleton;

public class SentFragment extends Fragment {
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String email = settings.getString(PREFS_MAIL, mailDefault);
        String pass = settings.getString(PREFS_PASSWORD, "password");
        if(settings.getString(PREFS_IV, null) != null) {
            iv = Base64.decode(settings.getString(PREFS_IV, null), Base64.DEFAULT);
        }
        MailHandlerIMAP handler = new MailHandlerIMAP(email, pass, iv);
        //Todo: put together "Gesendet" and "Sent"
        MessageSkeleton[] messages = reverse(handler.fetch("Gesendet"));
        System.out.println(messages.length);
        View root = inflater.inflate(R.layout.fragment_inbox, container, false);
        final TextView tw = root.findViewById(R.id.text_inbox);
        if(messages.length == 0) {
            tw.setText(getText(R.string.sent_fragment));
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

    //TODO: CommonUtils.java
    //BUG: Shuffles. Maybe sort by date?

    private MessageSkeleton[] reverse(MessageSkeleton[] toSort) {
        MessageSkeleton[] reversed = new MessageSkeleton[toSort.length];
        int i = toSort.length - 1;
        for(MessageSkeleton message : toSort) {
            reversed[i] = message;
            i--;
        }
        return reversed;
    }
}

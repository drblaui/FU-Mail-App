package me.drblau.fumailapp.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannedString;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import me.drblau.fumailapp.R;
import me.drblau.fumailapp.util.common.Settings;
import me.drblau.fumailapp.util.mail.MailHandlerIMAP;
import me.drblau.fumailapp.util.mail.MessageSkeleton;

public class MailDetail extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings settings = new Settings(this);
        MailHandlerIMAP handler = new MailHandlerIMAP(settings.getMail(), settings.getPassword(), settings.getIv());
        Intent intent = getIntent();
        int id = intent.getIntExtra("mailId", -1);
        String folder = intent.getStringExtra("folder");
        int notificationId = intent.getIntExtra("notificId", -1);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert mNotificationManager != null;
        mNotificationManager.cancel(notificationId);

        MessageSkeleton message = handler.fetch(folder, id);
        setContentView(R.layout.activity_mail_detail);

        TextView sender = findViewById(R.id.sender);
        sender.setText(getText(this, R.string.from, message.getFromToString()));

        TextView receiver = findViewById(R.id.receiver);
        receiver.setText(getText(this, R.string.to, settings.getMail()));

        TextView subject = findViewById(R.id.subject);
        subject.setText(getText(this, R.string.subject, message.getSubject()));

        TextView date = findViewById(R.id.date);
        Date messageDate = message.getSentDate();
        SimpleDateFormat formatter = new SimpleDateFormat("EE, MMMM dd, yyyy HH:mm", getResources().getConfiguration().locale);
        String dateStr = formatter.format(messageDate);

        date.setText(getText(this, R.string.date, dateStr));

        TextView content = findViewById(R.id.content);
        try {
            content.setText(message.getContent());
        }
        catch (Exception e) {
            content.setText("Error");
        }

        setTitle(folder + ": " + id);
        ActionBar actionBar = getSupportActionBar();

        //Enable getting out of intent
        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Bit of a hack, since I don't know the Button Id
        //But since there is only one Button in the ActionBar, we can ignore everything
        onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    public static CharSequence getText(Context context, int id, Object... args) {
        for(int i = 0; i < args.length; ++i)
            args[i] = args[i] instanceof String? TextUtils.htmlEncode((String)args[i]) : args[i];
        return removeTrailingLineFeed(Html.fromHtml(String.format(Html.toHtml(new SpannedString(context.getText(id))), args)));
    }

    private static CharSequence removeTrailingLineFeed(@NonNull CharSequence text) {
        while (text.charAt(text.length() - 1) == '\n' && text.length() > 0) {
            text = text.subSequence(0, text.length() - 1);
        }
        return text;
    }
}

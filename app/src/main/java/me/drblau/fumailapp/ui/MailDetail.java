package me.drblau.fumailapp.ui;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
        sender.setText(message.getFromToString());

        TextView receiver = findViewById(R.id.receiver);
        receiver.setText(settings.getMail());

        TextView subject = findViewById(R.id.subject);
        subject.setText(message.getSubject());

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
}

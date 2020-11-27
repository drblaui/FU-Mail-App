package me.drblau.fumailapp.util.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import me.drblau.fumailapp.R;
import me.drblau.fumailapp.ui.MailDetail;
import me.drblau.fumailapp.util.common.Settings;
import me.drblau.fumailapp.util.mail.MailHandlerIMAP;
import me.drblau.fumailapp.util.mail.MessageSkeleton;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationWorker extends Worker {
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    private final Context context;
    private int counter;
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        counter = 0;
    }


    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        MailHandlerIMAP handler = new MailHandlerIMAP(inputData.getString("mail"), inputData.getString("password"), inputData.getByteArray("iv"));
        try {
            MessageSkeleton[] messages = handler.fetch("Unread|Notify");
            for(MessageSkeleton message : messages) {
                createNotification(message.getFromToString(), message.getSubject(), message.getContent(), message.getFolder(), message.getId());

            }
            return Result.success();
        }
        catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }

    }

    private void createNotification (String sender, String subject, String content, String folder, int id) {
        int notifyId = (int) System.currentTimeMillis();
        Intent openIntent = new Intent(getApplicationContext(), MailDetail.class)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .putExtra("mailId", id)
            .putExtra("notificId", notifyId)
            .putExtra("folder", folder);

        Intent markReadIntent = new Intent(getApplicationContext(), ReadBroadcastReceiver.class)
                .putExtra("mailId", id)
                .putExtra("folder", folder)
                .putExtra("mail", getInputData().getString("mail"))
                .putExtra("pass", getInputData().getString("password"))
                .putExtra("iv", getInputData().getByteArray("iv"))
                .putExtra("notificId", notifyId);

        PendingIntent openPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, openIntent, 0);
        PendingIntent markReadPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, markReadIntent, 0);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE) ;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext() , default_notification_channel_id )
            .setContentTitle(sender)
            .setContentText(subject)
            .setTicker(subject)
            .setSmallIcon(R.drawable.logo)
            .setShowWhen(true)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_checkmark, getApplicationContext().getString(R.string.markRead), markReadPendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(content));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ) {
            int importance = NotificationManager.IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID , "New Emails" , importance) ;
            mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(notifyId, mBuilder.build()) ;
    }
}

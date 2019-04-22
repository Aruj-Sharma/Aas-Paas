package zero2unicorn.aaspaas.classes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import zero2unicorn.aaspaas.R;
import zero2unicorn.aaspaas.activity.MainActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String msg = "",title="";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().containsKey("body")) {
            msg= remoteMessage.getData().get("body");
        }
        if (remoteMessage.getData().containsKey("title")) {
            title= remoteMessage.getData().get("title");
        }
        sendMyNotifictaion();
    }

    private void sendMyNotifictaion() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("Aaspaas")
                .setContentText(title).setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);
    }
}

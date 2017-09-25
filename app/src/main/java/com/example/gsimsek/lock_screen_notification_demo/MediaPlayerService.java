package com.example.gsimsek.lock_screen_notification_demo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.media.session.MediaController;
import android.os.IBinder;

/**
 * Created by gsimsek on 24-Sep-17.
 */

public class MediaPlayerService extends Service
{

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        _mediaSession.release();
        return super.onUnbind(intent);
    }

    private void handleIntent(Intent intent)
    {
        if(null == intent || null == intent.getAction())
            return;

        String action = intent.getAction();

        if(action.equalsIgnoreCase(ACTION_PLAY))
            _mediaController.getTransportControls().play();
        else if(action.equalsIgnoreCase(ACTION_PAUSE))
            _mediaController.getTransportControls().pause();
        else if(action.equalsIgnoreCase(ACTION_FAST_FORWARD))
            _mediaController.getTransportControls().fastForward();
        else if(action.equalsIgnoreCase(ACTION_REWIND))
            _mediaController.getTransportControls().rewind();
        else if(action.equalsIgnoreCase(ACTION_PREVIOUS))
            _mediaController.getTransportControls().skipToPrevious();
        else if(action.equalsIgnoreCase(ACTION_NEXT))
            _mediaController.getTransportControls().skipToNext();
        else if(action.equalsIgnoreCase(ACTION_STOP))
            _mediaController.getTransportControls().stop();
    }

    private Notification.Action generateAction(int icon, String title, String intentAction)
    {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent =
                PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build(); // todo
    }

    private void buildNotification(Notification.Action action)
    {
        Notification.MediaStyle style = new Notification.MediaStyle();
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent =
                PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Lock Screen Media Example")
                .setContentText("Artist Name")
                .setDeleteIntent(pendingIntent)
                .setStyle(style);

        builder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous",
                ACTION_PREVIOUS));
        builder.addAction(generateAction(android.R.drawable.ic_media_rew, "Rewind", ACTION_REWIND));
        builder.addAction(action);
        builder.addAction(generateAction(android.R.drawable.ic_media_ff, "Fast Forward",
                ACTION_FAST_FORWARD));
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));

        //style.setShowActionsInCompactView(0, 1, 2, 3, 4);
        style.setShowActionsInCompactView(0, 2, 4);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(null == _mediaSessionManager)
            initMediaSession();

        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaSession()
    {
        _mediaPlayer = new MediaPlayer();
        _mediaSession = new MediaSession(getApplicationContext(), "example player session");
        _mediaController = new MediaController(getApplicationContext(), _mediaSession.getSessionToken());
        _mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                buildNotification((generateAction(android.R.drawable.ic_media_pause,
                        "Pause", ACTION_PAUSE)));
            }

            @Override
            public void onPause() {
                super.onPause();
                buildNotification(generateAction(android.R.drawable.ic_media_play,
                        "Pause", ACTION_PLAY)); // todo: use ACTION_PAUSE ?
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                buildNotification(generateAction(android.R.drawable.ic_media_pause,
                        "Pause", ACTION_PAUSE));
            }

            @Override
            public void onFastForward() {
                super.onFastForward();
            }

            @Override
            public void onRewind() {
                super.onRewind();
            }

            @Override
            public void onStop() {
                super.onStop();
                NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1);
                Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                stopService(intent);
            }
        });

    }

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_forward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    private MediaSession _mediaSession;
    private  MediaPlayer _mediaPlayer;
    private MediaSessionManager _mediaSessionManager;
    private MediaController _mediaController;


}

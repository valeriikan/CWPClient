package fi.oulu.tol.esde019.cwpclient019.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import fi.oulu.tol.esde019.cwpclient019.CWPProvider;
import fi.oulu.tol.esde019.cwpclient019.MainActivity;
import fi.oulu.tol.esde019.cwpclient019.R;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPControl;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPMessaging;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWProtocolListener;

public class CWPService extends Service implements CWPProvider, Observer {

    private CWPModel mCWPModel = null;
    private CWPBinder mCWPBinder = new CWPBinder();
    int counter = 0;
    Signaller signaller = null;
    private NotificationManager mNotificationManager;
    private String msg = "Disconnected";
    private final static String TAG = "CWP019";


    public CWPService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCWPModel = new CWPModel();
        mCWPModel.addObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mCWPModel.disconnect();
            mCWPModel = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mCWPBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public CWPMessaging getMessaging() {
        return mCWPModel;
    }

    @Override
    public CWPControl getControl() {
        return mCWPModel;
    }

    @Override
    public void update(Observable observable, Object o) {
        // updates notification string on events changed
        if (o.equals(CWProtocolListener.CWPEvent.EDisconnected)) {
            msg = getString(R.string.notify_disconnected);
        } else if (o.equals(CWProtocolListener.CWPEvent.EConnected)) {
            msg = getString(R.string.notify_connected);
        } else if (o.equals(CWProtocolListener.CWPEvent.ELineDown)) {
            msg = getString(R.string.notify_connected);
        } else if (o.equals(CWProtocolListener.CWPEvent.ELineUp)) {
            msg = getString(R.string.notify_signalling);
        }
        // recreates notification on events changed
        if (counter == 0) {
            buildNotify();
        }
    }

    public void startUsing() {
        counter++;
        Log.wtf(TAG, "Clients: " + counter);
        if (counter == 1) {
            if (signaller == null) {
                signaller = new Signaller();
                mCWPModel.addObserver(signaller);
                if (mCWPModel.lineIsUp()) {
                    signaller.start();
                }
            }
        }
        if (mNotificationManager != null) {
            mNotificationManager.cancel(1); //remove notification if app is moved to foreground
        }
    }

    public void stopUsing() {
        counter--;
        Log.wtf(TAG, "Clients: " + counter);
        if (counter == 0) {
            if (signaller != null) {
                signaller.stop();
                mCWPModel.deleteObserver(signaller);
                signaller = null;
                buildNotify(); //builds notification when app moved to background but CWP state is not changed
            }
        }
    }

    private void buildNotify() {
        Log.wtf(TAG, "Showing notification");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_white_24dp)
                .setContentTitle(getString(R.string.notify_title))
                .setContentText(msg);
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    public class CWPBinder extends Binder {
        public CWPService getService() {
            return CWPService.this;
        }
    }
}
/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */
package de.blinkt.openvpn.core;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

//import com.onesignal.OneSignal;
//import com.orm.SugarContext;

//import com.oasisfeng.condom.CondomProcess;
import com.wxy.vpn2018.R;

/*
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
*/
public class App extends /*com.orm.SugarApp*/ Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        CondomProcess.installExceptDefaultProcess(this);
        PRNGFixes.apply();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels();
        }
        StatusListener mStatus = new StatusListener();
        mStatus.init(getApplicationContext());
//        OneSignal.startInit(this).inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification).unsubscribeWhenNotificationsAreDisabled(true).init();
//        SugarContext.init(this);//初始化
    }

    public static boolean isStart;

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannels() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Background message
        CharSequence name = getString(R.string.channel_name_background);
        NotificationChannel mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_BG_ID, name, NotificationManager.IMPORTANCE_MIN);
        mChannel.setDescription(getString(R.string.channel_description_background));
        mChannel.enableLights(false);
        mChannel.setLightColor(Color.DKGRAY);
        mNotificationManager.createNotificationChannel(mChannel);
        // Connection status change messages
        name = getString(R.string.channel_name_status);
        mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
        mChannel.setDescription(getString(R.string.channel_description_status));
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        mNotificationManager.createNotificationChannel(mChannel);
    }

//    @Override
//    public void onTerminate() {
//        super.onTerminate();
//        SugarContext.terminate();//终止销毁
//    }
}

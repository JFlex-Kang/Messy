package devicewills.services;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.cracking.jflex.devicewilly.R;

import java.util.ArrayList;

import devicewills.activities.ActivityMenu;
import devicewills.utils.BatteryUtil;
import devicewills.objects.ContactInfo;
import devicewills.utils.PreferencesUtil;

/**
 * Created by Cracking on 2017-03-31.
 */

//백그라운드 베터리 용량 측정 서비스
public class WillService extends Service {

    private static String TAG_NULL = null;
    private ArrayList<ContactInfo> list;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    //서비스 시작 메소드
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG_NULL, "In onStartCommand");

        //filter로 베터리 용량의 변화를 감지
        final IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(mBatteryRecv, batteryIntentFilter);

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        Log.i(TAG_NULL, "In onDestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG_NULL, "In onBind");
        return null;
    }

    //베터리 용량 변화시 호출받는 리시버
    BroadcastReceiver mBatteryRecv = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkBatteryLevel(intent);
        }
    };

    //베터리 용량 측정
    private void checkBatteryLevel(Intent batteryChangeIntent) {
        // 베터리 퍼센트 계산
        BatteryUtil batteryUtil = new BatteryUtil();
        int percentage = batteryUtil.getPerc(batteryChangeIntent);

        if (percentage == PreferencesUtil.getIntPreferences(WillService.this, "bty_perc")) {
            list = PreferencesUtil.getArrayListPreferendes(WillService.this);
            for (int i = 0; i < list.size(); i++) {
                sendSms(list.get(i).getmContactNum(), PreferencesUtil.getPreferences(WillService.this, "bty_msg"));
            }
            unregisterReceiver(mBatteryRecv);
            unregisterReceiver(sendReceiver);

            stopService(new Intent(this, WillService.class));
        }
    }

    //메시지 전송 시 호출받는 리시버
    BroadcastReceiver sendReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    showNotification();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "전송 실패", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "서비스 지역이 아님", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "무선 (Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "PDU Null", Toast.LENGTH_SHORT).show();
                    break;
            }
            PreferencesUtil.setBoolPreferences(WillService.this, "isServiceRunning", false);
        }
    };

    private void sendSms(String num, String msg) {
        PendingIntent sendIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);

        IntentFilter filter = new IntentFilter("SMS_SENT_ACTION");
        registerReceiver(sendReceiver, filter);

        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(num, null, msg, sendIntent, null);
    }

    private void showNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        int id = R.drawable.icon_mail_send;
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), id));
        builder.setSmallIcon(id);
        builder.setTicker("메시지 전송 완료!");
        builder.setContentTitle("메시지 전송 완료");
        builder.setContentText("자세히");
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setAutoCancel(true);

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle(builder);
        inboxStyle.addLine(PreferencesUtil.getIntPreferences(getApplicationContext(), "bty_perc") + "% 가 되었으므로");
        if (list.size() > 1) {
            inboxStyle.addLine("예약하신 " + list.get(0).getmContactName() + "외  " + list.size() + " 명에게 메시지를 전송하였습니다.");
        } else {
            inboxStyle.addLine("예약하신 " + list.get(0).getmContactName() + " 에게 메시지를 전송하였습니다.");
        }
        builder.setStyle(inboxStyle);

        notificationManager.notify(0, builder.build());
    }
}

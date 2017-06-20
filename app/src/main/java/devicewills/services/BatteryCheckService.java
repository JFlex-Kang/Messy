package devicewills.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import devicewills.utils.BatteryUtil;

/**
 * Created by Cracking on 2017-03-31.
 */

//백그라운드 베터리 용량 측정 서비스
public class BatteryCheckService extends Service {

    private static String TAG_NULL = null;
    public static final String BROADCAST_ACTION = "BATTERY_CHECK";

    @Override
    public void onCreate() {
        super.onCreate();

    }

    //서비스 시작 메소드
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //filter로 베터리 용량의 변화를 감지
        final IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        this.registerReceiver(mBatteryRecv, batteryIntentFilter);

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
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

        //MenuActivity 브로드캐스트 리시버로 브로드캐스팅함
        Intent broadcastIntent = new Intent(BROADCAST_ACTION);
        broadcastIntent.putExtra("percentage", percentage);
        sendBroadcast(broadcastIntent);

    }
}

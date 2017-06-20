package devicewills.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Dell on 2017-05-24.
 */

public class ServiceRunningChecker {

    public Boolean isServiceRunning(String serviceName, Context mContext) {

        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = activityManager.getRunningServices(1000);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : rs) {

            if (serviceName.equals(runningServiceInfo.service.getClassName())) {
                return true;
            }

        }
        return false;
    }
}

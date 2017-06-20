package devicewills.utils;

import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Created by Cracking on 2017-03-31.
 */

//베터리 관련 클래스
public class BatteryUtil{
    //베터리 잔량 구하기
    public int getPerc(Intent intent){
        //베터리 최대량
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,100);
        Log.e("scale ", String.valueOf(scale));
        //베터리 잔량
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
        Log.e("level ", String.valueOf(level));
        //베터리 퍼센트
        int percent = (int)((float)level / (float)scale * 100);
        return percent;
    }
}

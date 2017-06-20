package devicewills.utils;

import android.text.TextUtils;

/**
 * Created by 9929k on 2017-04-10.
 */

public class FormValidator {

    public static boolean isValidMsg(String msg){
        return !TextUtils.isEmpty(msg) && msg.trim().length()<140;
    }
}

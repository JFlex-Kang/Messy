package devicewills.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Cracking on 2017-04-27.
 */

public class PrefManager {
    SharedPreferences mPref;
    SharedPreferences.Editor mEditor;
    Context mContext;

    int PRIVATE_MDOE = 0;

    private static final String PREF_NAME = "phone-will";
    private static final String IS_FIRST = "IsFirst";

    public PrefManager(Context context) {
        this.mContext = context;
        mPref = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MDOE);
        mEditor = mPref.edit();
    }

    public void setFirstLaunch(boolean isFirstTime) {
        mEditor.putBoolean(IS_FIRST, isFirstTime);
        mEditor.commit();
    }

    public boolean isFirstLaunch() {
        return mPref.getBoolean(IS_FIRST, true);
    }
}

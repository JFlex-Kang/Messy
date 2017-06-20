package devicewills.objects;

import java.util.Date;

/**
 * Created by Cracking on 2017-04-04.
 */

public class UserSettingProfile {
    private int mBatteryPerc;
    private String mMessage;
    private Date mInsertDate;

    private UserSettingProfile() {
    }

    public UserSettingProfile(int mBatteryPerc, String mMessage, Date mInsertDate) {
        this.mBatteryPerc = mBatteryPerc;
        this.mMessage = mMessage;
        this.mInsertDate = mInsertDate;
    }

    public int getmBatteryPerc() {
        return mBatteryPerc;
    }

    public void setmBatteryPerc(int mBatteryPerc) {
        this.mBatteryPerc = mBatteryPerc;
    }

    public String getmMessage() {
        return mMessage;
    }

    public void setmMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public Date getmInsertDate() {
        return mInsertDate;
    }

    public void setmInsertDate(Date mInsertDate) {
        this.mInsertDate = mInsertDate;
    }
}

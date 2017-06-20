package devicewills.objects;

import android.graphics.Bitmap;

/**
 * Created by 9929k on 2017-04-12.
 */

public class ContactInfo {
    private Bitmap mContactImg;
    private String mContactName;
    private String mContactNum;
    private boolean isChecked = false;

    public ContactInfo(String mContactName, String mContactNum, Bitmap mContactImg) {
        this.mContactName = mContactName;
        this.mContactNum = mContactNum;
        this.mContactImg = mContactImg;
    }

    public ContactInfo(String mContactName, String mContactNum) {
        this.mContactName = mContactName;
        this.mContactNum = mContactNum;
    }

    public ContactInfo() {
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    public Bitmap getmContactImg() {
        return mContactImg;
    }

    public void setmContactImg(Bitmap mContactImg) {
        this.mContactImg = mContactImg;
    }

    public String getmContactName() {
        return mContactName;
    }

    public void setmContactName(String mContactName) {
        this.mContactName = mContactName;
    }

    public String getmContactNum() {
        return mContactNum;
    }

    public void setmContactNum(String mContactNum) {
        this.mContactNum = mContactNum;
    }
}

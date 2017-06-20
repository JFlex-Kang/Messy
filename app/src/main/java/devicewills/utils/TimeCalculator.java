package devicewills.utils;

import java.util.Date;

/**
 * Created by Dell on 2017-05-05.
 */

public class TimeCalculator {
    private static class TiME_MAXIMUM {
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }

    public String calculateTime(Date date) {

        long curTime = System.currentTimeMillis();
        long regTime = date.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;

        if (diffTime < TiME_MAXIMUM.SEC) {
            msg = diffTime + "초전";
        } else if ((diffTime /= TiME_MAXIMUM.SEC) < TiME_MAXIMUM.MIN) {
            msg = diffTime + "분전";
        } else if ((diffTime /= TiME_MAXIMUM.MIN) < TiME_MAXIMUM.HOUR) {
            msg = diffTime + "시간전";
        } else if ((diffTime /= TiME_MAXIMUM.HOUR) < TiME_MAXIMUM.DAY) {
            msg = diffTime + "일전";
        } else if ((diffTime /= TiME_MAXIMUM.DAY) < TiME_MAXIMUM.MONTH) {
            msg = diffTime + "달전";
        } else {
            msg = diffTime + "년전";
        }
        return msg;
    }
}

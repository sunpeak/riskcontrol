package com.example.riskcontrol.model;

import java.util.Date;

/**
 * Created by sunpeak on 2016/8/6.
 */
public enum EnumTimePeriod {

    ALL,
    LASTMIN,
    LASTHOUR,
    LASTDAY;

    public Date getMinTime(Date now) {
        if (this.equals(ALL)) {
            return new Date(0);
        } else {
            return new Date(now.getTime() - getTimeDiff());
        }
    }

    public Date getMaxTime(Date now) {
        return now;
    }

    public long getTimeDiff() {
        long timeDiff;
        switch (this) {
            case ALL:
                timeDiff = Long.MAX_VALUE;
                break;
            case LASTMIN:
                timeDiff = 60 * 1000L;
                break;
            case LASTHOUR:
                timeDiff = 3600 * 1000L;
                break;
            case LASTDAY:
                timeDiff = 24 * 3600 * 1000L;
                break;
            default:
                timeDiff = 60 * 1000L;
                break;
        }
        return timeDiff;
    }
}

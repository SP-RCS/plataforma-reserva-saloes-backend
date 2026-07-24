package com.ucan.reservasaloes.config;

import java.util.Date;

public class DataUtils {

    public static int compare(Date d1, Date d2)
    {
        long dif = d1.getTime() - d2.getTime();
        return dif < 0 ? -1 : (dif == 0 ? 0 : 1);
    }

}

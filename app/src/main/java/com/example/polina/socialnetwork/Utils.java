package com.example.polina.socialnetwork;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by polina on 17.07.15.
 */
public class Utils {

    public static int calculateAmountYears(String birthday) {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date bDate = format.parse(birthday);
            int bYear = bDate.getYear();
            int bMonth = bDate.getMonth();
            int bDay = bDate.getDay();

            Date nowDate = now.getTime();
            int nowYear = nowDate.getYear();
            int nowMonth = nowDate.getMonth();
            int nowDay = nowDate.getDay();

            int year = nowYear - bYear - 1;

            if (bMonth < nowMonth) {
                year++;
            }
            if (bMonth == nowMonth) {
                if (bDay < nowDay) {
                    year++;
                }
            }

            return year;

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }

    }

}

package com.example.research2.SpringBoot.util;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;


public class TimezoneUtils {
    public static List<String> getAllTimezones() {
        String[] timezones = TimeZone.getAvailableIDs();
        Arrays.sort(timezones);
        return Arrays.asList(timezones);
    }
}

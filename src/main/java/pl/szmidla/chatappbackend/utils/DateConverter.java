package pl.szmidla.chatappbackend.utils;

import java.time.LocalDateTime;

public class DateConverter {

    public static String LocalDateTimeToShortString(LocalDateTime dateTime) {
        String wholeDateString = dateTime.toString();
        String shortDateString = wholeDateString.substring(0, wholeDateString.length() - 10); // remove nanoseconds
        return shortDateString;
    }
}

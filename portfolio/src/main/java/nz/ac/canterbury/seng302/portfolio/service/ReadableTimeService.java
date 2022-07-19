package nz.ac.canterbury.seng302.portfolio.service;

import com.google.protobuf.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class ReadableTimeService {

    /**
     * Gets a readable form of a date from a protobuf timestamp
     */
    public static String getReadableDate(Timestamp timestamp) {
        Date date = new Date(timestamp.getSeconds() * 1000); // Date needs milliseconds
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        return df.format(date);
    }

    /**
     * Gets the time since a timestamp in months and years
     */
    public static String getReadableTimeSince(Timestamp timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp.getSeconds(), 0, ZoneOffset.UTC);
        LocalDateTime now = LocalDateTime.now();


        long years = ChronoUnit.YEARS.between(dateTime, now);
        long months = ChronoUnit.MONTHS.between(dateTime, now) % 12;
        if (years > 0) {
            return years + " years, " + months + " months";
        } else {
            return months + " months";
        }


    }
}

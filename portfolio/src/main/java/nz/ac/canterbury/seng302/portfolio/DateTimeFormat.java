package nz.ac.canterbury.seng302.portfolio;

import java.time.format.DateTimeFormatter;

public class DateTimeFormat {

    public static DateTimeFormatter dayDateMonthYear() {
        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("E d MMMM y");
        return formatter;
    }
}

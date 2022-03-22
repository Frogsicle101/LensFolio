package nz.ac.canterbury.seng302.portfolio;

import java.time.format.DateTimeFormatter;

public class DateTimeFormat {

    public static DateTimeFormatter dayDateMonthYear() {
        return DateTimeFormatter.ofPattern("E d MMMM y");
    }

    public static DateTimeFormatter timeDateMonthYear() {
        return DateTimeFormatter.ofPattern("hh:mma E d MMMM y");
    }
}

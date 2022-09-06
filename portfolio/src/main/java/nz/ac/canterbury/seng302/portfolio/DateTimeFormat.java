package nz.ac.canterbury.seng302.portfolio;

import java.time.format.DateTimeFormatter;

public class DateTimeFormat {

    /**
     * Private constructor to prevent Java from creating an implicit public constructor -
     * this class should never be instantiated.
     */
    private DateTimeFormat() {
        throw new IllegalStateException("Utility class");
    }

    public static DateTimeFormatter dayDateMonthYear() {
        return DateTimeFormatter.ofPattern("E d MMMM y");
    }

    public static DateTimeFormatter timeDateMonthYear() {
        return DateTimeFormatter.ofPattern("hh:mma E d MMMM y");
    }

    public static DateTimeFormatter yearMonthDay() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    public static DateTimeFormatter dayMonthYear() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }
}

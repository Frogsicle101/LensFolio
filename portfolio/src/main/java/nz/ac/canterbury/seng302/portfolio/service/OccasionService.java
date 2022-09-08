package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;

import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * Static class to check if a date lies inside a project.
 */
public class OccasionService {

    /** To hide the public constructor as the methods of this class are static */
    private OccasionService() {}


    /**
     * Checks that the end date occurs between the project's start and end dates.
     *
     * @param project The project defining the earliest and latest dates the end date can be.
     * @param date The end date being validated.
     * @throws DateTimeException If the end date is before the project start or after the project end.
     */
    public static void validateDate(Project project, LocalDate date) throws DateTimeException {

        if (date.isAfter(project.getEndDate()) || date.isBefore(project.getStartDate())) {
            throw new DateTimeException("Date(s) must occur during the project");
        }
    }
}

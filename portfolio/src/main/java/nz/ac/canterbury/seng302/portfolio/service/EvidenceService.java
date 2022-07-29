package nz.ac.canterbury.seng302.portfolio.service;
import nz.ac.canterbury.seng302.portfolio.CheckException;
import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A utility class for more complex actions involving Evidence
 */
@Service
public class EvidenceService {

    static Pattern alpha = Pattern.compile("[a-zA-Z]");

    /**
     * Checks if the string is too short or matches the pattern provided
     * if either of these are true then it throws an exception
     *
     * @param string A string
     * @throws CheckException The exception to throw
     */
    public static void checkString(String string) throws CheckException {
        Matcher matcher = alpha.matcher(string);
        if (string.length() < 2) {
            throw new CheckException("Title should be longer than 1 character");
        } else if (!matcher.find()){
            throw new CheckException("Title shouldn't be strange");
        }
    }
}

package nz.ac.canterbury.seng302.portfolio.service;

import java.util.regex.Pattern;

/**
 * The enum to register the options for regex checks on user input
 */
public enum RegexPattern {

    /** Regex that is all unicode letters, numbers, punctuation, symbols and whitespace */
    GENERAL_UNICODE(Pattern.compile("[\\p{L}\\p{Nd}\\p{P}\\p{S}\\s]*", Pattern.CASE_INSENSITIVE),
            " can only contain unicode letters, numbers, punctuation, symbols and whitespace"),

    /** Regex that is all unicode letters, dashes and spaces. Intended for first, middle and last names */
    REAL_NAME(Pattern.compile("[\\p{L}\\p{Pd}\\p{Zs}]*", Pattern.CASE_INSENSITIVE),
            " can only contain letters, dashes and spaces."),

    /** Regex that is all unicode letters, numbers, punctuation & symbols. Intended for usernames and passwords */
    GENERAL_UNICODE_NO_SPACES(Pattern.compile("[\\p{L}\\p{Nd}\\p{P}\\p{S}]*", Pattern.CASE_INSENSITIVE),
            " can only contain letters, numbers, punctuation and symbols."),

    EMAIL(Pattern.compile("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)+$"),
            " must be of a valid email format e.g., example@email.com"),
    ;

    private final Pattern pattern;

    private final String requirements;


    public Pattern getPattern() {
        return pattern;
    }

    public String getPatternString() {
        return pattern.toString();
    }

    public String getRequirements() {
        return requirements;
    }

    RegexPattern(Pattern pattern, String failureMessage) {
        this.pattern = pattern;
        this.requirements = failureMessage;
    }
}

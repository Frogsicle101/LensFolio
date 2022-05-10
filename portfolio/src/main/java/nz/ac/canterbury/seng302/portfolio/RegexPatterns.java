package nz.ac.canterbury.seng302.portfolio;

import java.util.regex.Pattern;

public class RegexPatterns {

    private final Pattern titleRegex = Pattern.compile("([a-zA-Z0-9_]+\\s?)+");
    private final Pattern descriptionRegex = Pattern.compile("([a-zA-Z0-9.,'\"]*\s?)+");
    private final Pattern hexRegex = Pattern.compile("#[0-9A-Fa-f]{1,6}");


    public Pattern getTitleRegex() {
        return titleRegex;
    }

    public Pattern getDescriptionRegex() {
        return descriptionRegex;
    }

    public Pattern getHexRegex() {
        return hexRegex;
    }
}

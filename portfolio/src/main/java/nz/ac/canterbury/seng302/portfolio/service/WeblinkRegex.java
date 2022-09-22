package nz.ac.canterbury.seng302.portfolio.service;

import java.util.regex.Pattern;

public class WeblinkRegex {
    //from rfc https://www.ietf.org/rfc/rfc2396.txt
    private static final String ESCAPED = "(%[0-9a-f][0-9a-f])";
    private static final String RESERVED = ";\\/\\?:@&=\\+\\$,";
    private static final String UNRESERVED = "a-zA-Z0-9[-]_\\.!~\\*'\\(\\)";
    private static final String URIC = RESERVED + UNRESERVED + ESCAPED;

    private static final String PATH_CHARS = "a-zA-Z0-9[-]\\._~!\\$&'\\(\\)\\*\\+,;=:@" + ESCAPED;

    private static final String PROTOCOL = "(https?://)?";
    private static final String DOMAIN = "([a-zA-Z0-9\\-~_]+(\\.[a-zA-Z0-9\\-~_]+)*)";
    private static final String PORT = "(:[0-9]{0,5})?";
    private static final String PATH = "(/[%s]*)*".formatted(PATH_CHARS);
    private static final String QUERY = "(\\?(([%s]+=[%<s]+)+(&[%<s]+=[%<s]+)*)+)?".formatted(URIC);
    private static final String FRAGMENT = "(#[%s]*)?".formatted(URIC);

    private static final Pattern WEBLINK = Pattern.compile(
            PROTOCOL +
            DOMAIN +
            PORT +
            PATH +
            QUERY +
            FRAGMENT
    );

    private WeblinkRegex() {}

    public static Pattern getWeblinkPattern() {
        return WEBLINK;
    }
}

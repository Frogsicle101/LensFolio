package nz.ac.canterbury.seng302.portfolio.service;


import java.util.regex.Pattern;

public class WeblinkRegex {

    private static final String[] ALLOWED_PROTOCOLS = {"http", "https", "ftp"};

    //from rfc https://www.ietf.org/rfc/rfc2396.txt
    private static final String ESCAPED = "(%[\\da-f][\\da-f])";
    private static final String RESERVED = ";\\/\\?:@&=\\+\\$,";
    private static final String UNRESERVED = "\\w[-]\\.!~\\*'\\(\\)";
    private static final String URIC = RESERVED + UNRESERVED + ESCAPED;

    private static final String PATH_CHARS = "\\w[-]\\.~!\\$&'\\(\\)\\*\\+,;=:@/" + ESCAPED;

    private static final String PROTOCOL = "((%s)://)".formatted(String.join("|", ALLOWED_PROTOCOLS));
    private static final String DOMAIN = "([\\w\\-~_]+(\\.[\\w\\-~]+)*)";
    private static final String PORT = "(:\\d{0,5})?";
    private static final String PATH = "(/(%s)*)*".formatted(PATH_CHARS);
    private static final String QUERY = "(\\?(((%s)+=(%<s)+)+(&(%<s)+=(%<s)+)*)+)?".formatted(URIC);
    private static final String FRAGMENT = "(#[%s]*)?".formatted(URIC);

    private static final Pattern WEBLINK = Pattern.compile(
                    PROTOCOL +
                    DOMAIN +
                    PORT +
                    PATH +
                    QUERY +
                    FRAGMENT +
                    "|" +
                    DOMAIN +
                    PATH

    );

    private WeblinkRegex() {}

    public static Pattern getWeblinkPattern() {
        return WEBLINK;
    }
}

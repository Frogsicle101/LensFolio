package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.service.WeblinkRegex;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegexTests {

    private final Pattern weblinkPattern = WeblinkRegex.getWeblinkPattern();
    private final List<String> expectedPasses = new ArrayList<>();
    private final List<String> expectedFails = new ArrayList<>();

    private void assertPasses() {
        for (String test : expectedPasses) {
            if (! weblinkPattern.matcher(test).matches()) {
                Assertions.fail("Expected " + test + " to match weblink regex, but didn't");
            }
        }
    }

    private void assertFails() {
        for (String test : expectedFails) {
            if (weblinkPattern.matcher(test).matches()) {
                Assertions.fail("Expected " + test + " to not match weblink regex, but did");
            }
        }
    }

    @Test
    public void regexSchemePasses() {
        expectedPasses.add("example");
        expectedPasses.add("http://example");
        expectedPasses.add("http://example.com"); // from RFC documentation
        expectedPasses.add("http://example.com/"); // from RFC documentation
        expectedPasses.add("http://example.com:/"); // from RFC documentation
        expectedPasses.add("http://example.com:80/"); // from RFC documentation

        assertPasses();
    }

    @Test
    public void regexPortPasses() {
        expectedPasses.add("http://example:");
        expectedPasses.add("http://example:80");
        expectedPasses.add("http://example:80000");

        assertPasses();
    }

    @Test
    public void regexPathPasses() {
        expectedPasses.add("https://example/");
        expectedPasses.add("http://example/path");
        expectedPasses.add("http://example:80000/path");
        expectedPasses.add("http://www.w3.org/Addressing/"); // from RFC documentation
        expectedPasses.add("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        expectedPasses.add("http://localhost:9000/portfolio?projectId=1");
        expectedPasses.add("http://hello/path/lots-of_chars_T0~.-()test");
        expectedPasses.add("https://learn.canterbury.ac.nz/login/index.php");
        expectedPasses.add("https://stackoverflow.com/questions/13009670/prolog-recursive-list-construction");

        assertPasses();
    }

    @Test
    public void regexQueryPasses() {
        expectedPasses.add("http://example?a=a");
        expectedPasses.add("http://example?sam=nerd");
        expectedPasses.add("http://example?sam=nerd&april=100TimesAsSwag&harrison=27");

        assertPasses();
    }

    @Test
    public void regexFragmentPasses() {
        expectedPasses.add("https://example#3456");
        expectedPasses.add("https://example#34sdfg-';");
        expectedPasses.add("http://www.ics.uci.edu/pub/ietf/uri/historical.html#WARNING"); // from RFC documentation

        assertPasses();
    }

    @Test
    public void regexTestsFail() {
        // Protocol
        expectedFails.add(".");
        expectedFails.add("://example");
        expectedFails.add("htt://example");
        expectedFails.add("htp://example");
        expectedFails.add("http:/example");
        expectedFails.add("http:///example");
        expectedFails.add("https:///example");

        // Domain
        expectedFails.add(".example");
        expectedFails.add("http://.example");

        // port number
        expectedFails.add("example:800000");
        expectedFails.add("http://example:800000");

        expectedFails.add("https://");
        expectedFails.add("hps://");
        expectedFails.add("https:/example");
        expectedFails.add("https://example?");
        expectedFails.add("https://example:");

        assertFails();
    }

    @Test
    public void questionablePassCases() {
        List<String> questionablePasses = new ArrayList<>();

        questionablePasses.add("https//:example");
        questionablePasses.add("http//example"); // http is considered part of the domain

        for (String test : questionablePasses) {
            if (! weblinkPattern.matcher(test).matches()) {
                Assertions.fail("Expected " + test + " to not match weblink regex, but did");
            }
        }
    }
}
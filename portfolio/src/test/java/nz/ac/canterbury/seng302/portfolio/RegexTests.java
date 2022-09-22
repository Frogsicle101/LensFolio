package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.service.WeblinkRegex;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
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
    public void regexProtocolPasses() {
        expectedPasses.add("hello");
        expectedPasses.add("http://hello");
        expectedPasses.add("https://hello");

        assertPasses();
    }

    @Test
    public void regexPortPasses() {
        expectedPasses.add("http://hello:80");
        expectedPasses.add("http://hello:80000");

        assertPasses();
    }

    @Test
    public void regexPathPasses() {
        expectedPasses.add("https://hello/");
        expectedPasses.add("http://hello/path");
        expectedPasses.add("http://hello:80000/path");
        expectedPasses.add("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        expectedPasses.add("http://localhost:9000/portfolio?projectId=1");
        expectedPasses.add("http://hello/path/lots-of_chars_T0~.-()test");
        expectedPasses.add("https://learn.canterbury.ac.nz/login/index.php");
        expectedPasses.add("https://stackoverflow.com/questions/13009670/prolog-recursive-list-construction");

        assertPasses();
    }

    @Test
    public void regexQueryPasses() {
        expectedPasses.add("http://hello?a=a");
        expectedPasses.add("http://hello?sam=nerd");
        expectedPasses.add("http://hello?sam=nerd&april=100TimesAsSwag&harrison=27");

        assertPasses();
    }

    @Test
    public void regexFragmentPasses() {
        expectedPasses.add("https://hello#3456");
        expectedPasses.add("https://hello#34sdfg-';");

        assertPasses();
    }

    @Test
    public void regexTestsFail() {
        // Protocol
        expectedFails.add(".");
        expectedFails.add("://hello");
        expectedFails.add("htt://hello");
        expectedFails.add("htp://hello");
        expectedFails.add("http:/hello");
        expectedFails.add("http:///hello");
        expectedFails.add("https:///hello");

        // Domain
        expectedFails.add(".hello");
        expectedFails.add("http://.hello");

        // port number
        expectedFails.add("hello:800000");
        expectedFails.add("http://hello:800000");
        expectedFails.add("http://hello:800000");

        expectedFails.add("https://");
        expectedFails.add("hps://");
        expectedFails.add("https:/hello");
        expectedFails.add("https://hello?");
        expectedFails.add("https://hello:");

        assertFails();
    }

    @Test
    public void questionablePassCases() {
        List<String> questionablePasses = new ArrayList<>();

        questionablePasses.add("http//hello"); // fails as http is considered part of the domain

        for (String test : questionablePasses) {
            if (! weblinkPattern.matcher(test).matches()) {
                Assertions.fail("Expected " + test + " to not match weblink regex, but did");
            }
        }
    }
}
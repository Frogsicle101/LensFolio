package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.service.WeblinkRegex;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegexTests {

    private final Pattern weblinkPattern = WeblinkRegex.getWeblinkPattern();

    @Test
    public void regexTestsPasses() {
        List<String> expectedPasses = new ArrayList<>();

        // protocol
        expectedPasses.add("hello");
        expectedPasses.add("http://hello");
        expectedPasses.add("https://hello");


        expectedPasses.add("http://hello:80");
        expectedPasses.add("http://hello:80000");
        expectedPasses.add("hello");
        expectedPasses.add("https://hello");
        expectedPasses.add("http://hello");
        expectedPasses.add("http://hello?sam=nerd");
        expectedPasses.add("http://hello?sam=nerd&april=100TimesAsSwag&harrison=27");

        expectedPasses.add("http://hello/path");
        expectedPasses.add("http://hello/path/lots-of_chars_T0~.-()test");
        expectedPasses.add("http://hello:80000/path");
        expectedPasses.add("https://hello/");
        expectedPasses.add("https://hello#3456");
        expectedPasses.add("https://hello#34sdfg-';");

        for (String test : expectedPasses) {
            if (! weblinkPattern.matcher(test).matches()) {
                Assertions.fail("Expected " + test + " to match weblink regex, but didn't");
            }
        }
    }


    @Test
    public void regexTestsFail() {
        List<String> expectedFails = new ArrayList<>();
        // Protocol
        expectedFails.add("htp://hello");
        expectedFails.add("://hello");
        expectedFails.add("http:/hello");
//        expectedFails.add("http//hello"); // fails as http is considered part of the domain
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

        for (String test : expectedFails) {
            if (weblinkPattern.matcher(test).matches()) {
                Assertions.fail("Expected " + test + " to not match weblink regex, but did");
            }
        }
    }
}
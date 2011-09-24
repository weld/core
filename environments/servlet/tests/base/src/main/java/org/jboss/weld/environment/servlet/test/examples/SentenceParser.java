package org.jboss.weld.environment.servlet.test.examples;

import java.util.Arrays;
import java.util.List;

public class SentenceParser {
    public List<String> parse(String text) {
        return Arrays.asList(text.split("[.?]"));
    }
}

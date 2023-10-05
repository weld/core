package org.jboss.weld.examples.translator;

import java.util.Arrays;
import java.util.List;

import jakarta.enterprise.context.Dependent;

@Dependent
public class SentenceParser {

    public List<String> parse(String text) {
        return Arrays.asList(text.split("[.?]"));
    }

}

package org.jboss.weld.examples.translator;

import jakarta.enterprise.context.Dependent;

import java.util.Arrays;
import java.util.List;

@Dependent
public class SentenceParser {

    public List<String> parse(String text) {
        return Arrays.asList(text.split("[.?]"));
    }

}

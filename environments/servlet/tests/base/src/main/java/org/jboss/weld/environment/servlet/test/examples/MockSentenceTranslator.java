package org.jboss.weld.environment.servlet.test.examples;

import javax.enterprise.inject.Alternative;

@Alternative
public class MockSentenceTranslator implements Translator {
    public String translate(String sentence) {
        return "Lorem ipsum dolor sit amet";
    }
}

package org.jboss.weld.environment.servlet.test.examples;

import jakarta.inject.Inject;

public class TextTranslator {

    private SentenceParser sentenceParser;
    private Translator sentenceTranslator;

    @Inject
    TextTranslator(SentenceParser sentenceParser, Translator sentenceTranslator) {
        this.sentenceParser = sentenceParser;
        this.sentenceTranslator = sentenceTranslator;
    }

    public String translate(String text) {
        StringBuilder sb = new StringBuilder();
        for (String sentence : sentenceParser.parse(text)) {
            sb.append(sentenceTranslator.translate(sentence)).append(". ");
        }
        return sb.toString().trim();
    }

}

package org.jboss.weld.examples.translator;

import java.io.Serializable;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class TextTranslator implements Serializable {
    private SentenceParser sentenceParser;

    @EJB
    Translator translator;

    @Inject
    public TextTranslator(SentenceParser sentenceParser) {
        this.sentenceParser = sentenceParser;
    }

    public String translate(String text) {
        StringBuilder sb = new StringBuilder();
        for (String sentence : sentenceParser.parse(text)) {
            sb.append(translator.translate(sentence)).append(". ");
        }
        return sb.toString().trim();
    }

}

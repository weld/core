package org.jboss.weld.examples.translator;

public interface TranslatorController {

    String getText();

    void setText(String text);

    void translate();

    String getTranslatedText();

    void remove();

}

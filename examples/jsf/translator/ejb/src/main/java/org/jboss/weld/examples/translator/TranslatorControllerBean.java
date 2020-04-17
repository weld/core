package org.jboss.weld.examples.translator;

import jakarta.ejb.Remove;
import jakarta.ejb.Stateful;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Stateful
@RequestScoped
@Named("translator")
public class TranslatorControllerBean implements TranslatorController {

    @Inject
    private TextTranslator translator;

    private String inputText;

    private String translatedText;

    public String getText() {
        return inputText;
    }

    public void setText(String text) {
        this.inputText = text;
    }

    public void translate() {
        translatedText = translator.translate(inputText);
    }

    public String getTranslatedText() {
        return translatedText;
    }

    @Remove
    public void remove() {

    }

}

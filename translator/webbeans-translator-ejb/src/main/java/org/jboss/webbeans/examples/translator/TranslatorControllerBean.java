package org.jboss.webbeans.examples.translator;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.Current;
import javax.webbeans.Named;
import javax.webbeans.RequestScoped;

@Stateful
@RequestScoped
@Named("translator")
public class TranslatorControllerBean implements TranslatorController
{
   
   @Current TextTranslator translator;
   
   private String inputText;
   
   private String translatedText;
   
   public String getText()
   {
      return inputText;
   }
   
   public void setText(String text)
   {
      this.inputText = text;
   }
   
   public void translate()
   {
      translatedText = translator.translate(inputText);
   }
   
   public String getTranslatedText()
   {
      return translatedText;
   }
   
   @Remove
   public void remove()
   {
      
   }
   
}

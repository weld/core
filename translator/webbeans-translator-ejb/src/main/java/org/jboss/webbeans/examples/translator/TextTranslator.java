package org.jboss.webbeans.examples.translator;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.webbeans.Initializer;

public class TextTranslator implements Serializable
{ 
   private SentenceParser sentenceParser; 
   
   @EJB Translator translator;
   
   @Initializer
   TextTranslator(SentenceParser sentenceParser) 
   { 
      this.sentenceParser = sentenceParser;  
   }
   
   public String translate(String text) 
   { 
      StringBuilder sb = new StringBuilder(); 
      for (String sentence: sentenceParser.parse(text)) 
      { 
         sb.append(translator.translate(sentence)).append(". "); 
      } 
      return sb.toString().trim(); 
   }
   
}
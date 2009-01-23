package org.jboss.webbeans.examples.translator;

import java.io.Serializable;

import javax.webbeans.Initializer;

public class TextTranslator implements Serializable
{ 
   private SentenceParser sentenceParser; 
   private Translator sentenceTranslator; 
   
   @Initializer
   TextTranslator(SentenceParser sentenceParser, Translator sentenceTranslator) 
   { 
      this.sentenceParser = sentenceParser; 
      this.sentenceTranslator = sentenceTranslator; 
   }
   
   public String translate(String text) 
   { 
      StringBuilder sb = new StringBuilder(); 
      for (String sentence: sentenceParser.parse(text)) 
      { 
         sb.append(sentenceTranslator.translate(sentence)).append(". "); 
      } 
      return sb.toString().trim(); 
   }
   
}
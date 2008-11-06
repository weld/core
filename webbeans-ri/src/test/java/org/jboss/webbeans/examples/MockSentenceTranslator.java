package org.jboss.webbeans.examples;

@Mock
public class MockSentenceTranslator implements Translator { 
   public String translate(String sentence) { 
      return "Lorem ipsum dolor sit amet"; 
   }
} 

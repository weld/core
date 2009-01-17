package org.jboss.webbeans.test.examples;

@Mock
public class MockSentenceTranslator implements Translator { 
   public String translate(String sentence) { 
      return "Lorem ipsum dolor sit amet"; 
   }
} 

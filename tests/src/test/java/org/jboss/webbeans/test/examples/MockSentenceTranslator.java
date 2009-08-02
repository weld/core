package org.jboss.webbeans.test.examples;

import javax.enterprise.inject.Policy;

@Policy
public class MockSentenceTranslator implements Translator { 
   public String translate(String sentence) { 
      return "Lorem ipsum dolor sit amet"; 
   }
} 

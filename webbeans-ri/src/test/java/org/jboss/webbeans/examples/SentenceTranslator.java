package org.jboss.webbeans.examples;

import javax.ejb.Stateless;

@Stateless
public class SentenceTranslator implements Translator { 
public String translate(String sentence) { 
   throw new UnsupportedOperationException();
} 
}
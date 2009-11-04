package org.jboss.weld.tests.examples;

import javax.ejb.Local;

@Local 
public interface Translator { 
   public String translate(String sentence); 
}

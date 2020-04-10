package org.jboss.weld.examples.translator;

import jakarta.ejb.Local;

@Local
public interface Translator {

    String translate(String sentence);

}

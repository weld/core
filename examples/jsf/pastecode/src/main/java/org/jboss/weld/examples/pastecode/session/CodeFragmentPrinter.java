package org.jboss.weld.examples.pastecode.session;

import javax.ejb.Local;

@Local
public interface CodeFragmentPrinter {

    void startTimer();

}

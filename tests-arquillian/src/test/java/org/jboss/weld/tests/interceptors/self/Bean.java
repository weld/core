package org.jboss.weld.tests.interceptors.self;

import jakarta.enterprise.context.Dependent;

/**
 * @author Marius Bogoevici
 */
@Dependent
public class Bean implements Decorated {
    
    public void doUnintercepted() {
       doIntercepted();
       doDecorated();
    }

    @Secured
    public void doIntercepted() {
       // do nothing
    }

    public void doDecorated() {
        // do nothing
    }
}

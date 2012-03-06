package org.jboss.weld.tests.interceptors.self;

/**
 * @author Marius Bogoevici
 */
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

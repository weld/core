package org.jboss.weld.examples.pastecode.session;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.logging.Logger;

/**
 * Support for injecting a JDK logger. Uses the class name of the injecting
 * class as the category.
 *
 * @author Pete Muir
 */
public class LogManager {

    @Produces
    public Logger getLogger(InjectionPoint ip) {
        String category = ip.getMember().getDeclaringClass().getName();
        return Logger.getLogger(category);
    }

}

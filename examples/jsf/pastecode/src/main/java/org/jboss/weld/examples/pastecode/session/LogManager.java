package org.jboss.weld.examples.pastecode.session;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import java.util.logging.Logger;

/**
 * Support for injecting a JDK logger. Uses the class name of the injecting
 * class as the category.
 *
 * @author Pete Muir
 */
@Dependent
public class LogManager {

    @Produces
    public Logger getLogger(InjectionPoint ip) {
        String category = ip.getMember().getDeclaringClass().getName();
        return Logger.getLogger(category);
    }

}

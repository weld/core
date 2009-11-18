/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jboss.weld.tests.decorators.stackoverflow;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class that produces Logger for a given be to injected class
 *
 * @author wayne
 */
public class LogFacatory {
    @Produces
    public Logger getLogger(InjectionPoint ip){
        return LoggerFactory.getLogger(ip.getMember().getDeclaringClass());
    }
}

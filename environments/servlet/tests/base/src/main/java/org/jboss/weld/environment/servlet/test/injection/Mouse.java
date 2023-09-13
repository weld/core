package org.jboss.weld.environment.servlet.test.injection;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.inject.spi.BeanManager;

public class Mouse {

    public BeanManager getManager() {
        try {
            return (BeanManager) new InitialContext().lookup("java:comp/env/BeanManager");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

}

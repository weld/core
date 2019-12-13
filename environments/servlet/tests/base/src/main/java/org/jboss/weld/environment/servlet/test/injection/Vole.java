package org.jboss.weld.environment.servlet.test.injection;

import javax.annotation.Resource;
import jakarta.enterprise.inject.spi.BeanManager;

public class Vole {

    @Resource(mappedName = "java:comp/env/BeanManager")
    BeanManager manager;

    public BeanManager getManager() {
        return manager;
    }

}

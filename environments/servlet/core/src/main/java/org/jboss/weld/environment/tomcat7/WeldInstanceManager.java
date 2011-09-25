package org.jboss.weld.environment.tomcat7;

import org.apache.tomcat.InstanceManager;
import org.jboss.weld.environment.servlet.inject.AbstractInjector;
import org.jboss.weld.manager.api.WeldManager;

import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;

public class WeldInstanceManager extends AbstractInjector implements InstanceManager {

    protected WeldInstanceManager(WeldManager manager) {
        super(manager);
    }

    public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
    }

    public Object newInstance(String fqcn) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        return null;
    }

    public void newInstance(Object o) throws IllegalAccessException, InvocationTargetException, NamingException {
        inject(o);
    }

    public Object newInstance(String fqcn, ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        return null;
    }

}

package org.jboss.weld.environment.tomcat7;

import org.apache.tomcat.InstanceManager;

import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;

public abstract class ForwardingInstanceManager implements InstanceManager {

    protected abstract InstanceManager delegate();

    public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
        delegate().destroyInstance(o);

    }

    public void newInstance(Object o) throws IllegalAccessException, InvocationTargetException, NamingException {
        delegate().newInstance(o);
    }

    public Object newInstance(String fqcn, ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        return delegate().newInstance(fqcn, classLoader);
    }

    public Object newInstance(String fqcn) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        return delegate().newInstance(fqcn);
    }

}
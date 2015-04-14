package org.jboss.weld.environment.servlet.services;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.weld.environment.servlet.logging.WeldServletLogger;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.injection.spi.helpers.AbstractResourceServices;

public abstract class ServletResourceInjectionServices extends AbstractResourceServices implements ResourceInjectionServices {

    private static final String RESOURCE_LOOKUP_PREFIX = "java:comp/env";

    private Context context;

    public ServletResourceInjectionServices() {
        try {
            context = new InitialContext();
        } catch (NamingException e) {
            throw WeldServletLogger.LOG.errorCreatingJNDIContext(e);
        }
    }

    @Override
    protected Context getContext() {
        return context;
    }

    /*
     * The following methods are overridden to fix WELD-1920
     * Once the Weld SPI is fixed these changes are no longer necessary.
     */

    @Override
    public Object resolveResource(InjectionPoint injectionPoint) {
        if (getResourceAnnotation(injectionPoint) == null) {
            throw new IllegalArgumentException("No @Resource annotation found on injection point " + injectionPoint);
        }
        if (injectionPoint.getMember() instanceof Method && ((Method) injectionPoint.getMember()).getParameterTypes().length != 1) {
            throw new IllegalArgumentException(
                    "Injection point represents a method which doesn't follow JavaBean conventions (must have exactly one parameter) " + injectionPoint);
        }
        String name = getResourceName(injectionPoint);
        try {
            return getContext().lookup(name);
        } catch (NamingException e) {
            return handleNamingException(e, name);
        }
    }

    @Override
    protected String getResourceName(InjectionPoint injectionPoint) {
        Resource resource = getResourceAnnotation(injectionPoint);
        String mappedName = resource.mappedName();
        if (!mappedName.equals("")) {
            return mappedName;
        }
        String name = resource.name();
        if (!name.equals("")) {
            return RESOURCE_LOOKUP_PREFIX + "/" + name;
        }
        String propertyName;
        if (injectionPoint.getMember() instanceof Field) {
            propertyName = injectionPoint.getMember().getName();
        } else if (injectionPoint.getMember() instanceof Method) {
            propertyName = getPropertyName((Method) injectionPoint.getMember());
            if (propertyName == null) {
                throw new IllegalArgumentException("Injection point represents a method which doesn't follow "
                        + "JavaBean conventions (unable to determine property name) " + injectionPoint);
            }
        } else {
            throw new AssertionError("Unable to inject into " + injectionPoint);
        }
        String className = injectionPoint.getMember().getDeclaringClass().getName();
        return RESOURCE_LOOKUP_PREFIX + "/" + className + "/" + propertyName;
    }

    protected Resource getResourceAnnotation(InjectionPoint injectionPoint) {
        Annotated annotated = injectionPoint.getAnnotated();
        if (annotated instanceof AnnotatedParameter<?>) {
            annotated = ((AnnotatedParameter<?>) annotated).getDeclaringCallable();
        }
        return annotated.getAnnotation(Resource.class);
    }

    private Object handleNamingException(NamingException e, String name) {
        throw new RuntimeException("Error looking up " + name + " in JNDI", e);
    }

}

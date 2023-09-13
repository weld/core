package org.jboss.weld.environment.tomcat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.naming.NamingException;

import jakarta.servlet.ServletContext;

import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.InstanceManager;
import org.jboss.weld.environment.servlet.logging.TomcatLogger;
import org.jboss.weld.environment.util.Reflections;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.util.collections.Arrays2;

/**
 * Forwards all calls in turn to two delegates: first to InstanceManager, then to WeldInstanceManager
 *
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 */
public class WeldForwardingInstanceManager extends ForwardingInstanceManager {

    private static final String CONTEXT_FIELD_NAME = "context";

    private static final String INSTANCE_MANAGER_SETTER_NAME = "setInstanceManager";

    private static final String INSTANCE_MANAGER_GETTER_NAME = "getInstanceManager";

    private static final String INSTANCE_MANAGER_FIELD_NAME = "instanceManager";

    private final InstanceManager firstProcessor;

    private final InstanceManager secondProcessor;

    public WeldForwardingInstanceManager(InstanceManager originalAnnotationProcessor, InstanceManager weldProcessor) {
        this.firstProcessor = originalAnnotationProcessor;
        this.secondProcessor = weldProcessor;
    }

    @Override
    protected InstanceManager delegate() {
        return firstProcessor;
    }

    @Override
    public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
        super.destroyInstance(o);
        secondProcessor.destroyInstance(o);
    }

    @Override
    public void newInstance(Object o) throws IllegalAccessException, InvocationTargetException, NamingException {
        super.newInstance(o);
        secondProcessor.newInstance(o);
    }

    @Override
    public Object newInstance(String fqcn, ClassLoader classLoader)
            throws IllegalAccessException, InvocationTargetException, NamingException,
            InstantiationException, ClassNotFoundException, NoSuchMethodException {
        Object a = super.newInstance(fqcn, classLoader);
        secondProcessor.newInstance(a);
        return a;
    }

    @Override
    public Object newInstance(String fqcn)
            throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException,
            ClassNotFoundException, NoSuchMethodException {
        Object a = super.newInstance(fqcn);
        secondProcessor.newInstance(a);
        return a;
    }

    @Override
    public Object newInstance(Class<?> clazz) throws IllegalAccessException, InvocationTargetException, NamingException,
            InstantiationException, NoSuchMethodException {
        Object a = super.newInstance(clazz);
        secondProcessor.newInstance(a);
        return a;
    }

    public static void replaceInstanceManager(ServletContext context, WeldManager manager) {
        StandardContext stdContext = getStandardContext(context);
        setInstanceManager(stdContext, createInstance(manager, stdContext));
    }

    private static WeldForwardingInstanceManager createInstance(WeldManager manager, StandardContext stdContext) {
        try {
            InstanceManager weldProcessor = new WeldInstanceManager(manager);
            return new WeldForwardingInstanceManager(getInstanceManager(stdContext), weldProcessor);
        } catch (Exception e) {
            throw TomcatLogger.LOG.cannotCreatWeldForwardingAnnotationProcessor(e);
        }
    }

    private static StandardContext getStandardContext(ServletContext context) {
        try {
            // Hack into Tomcat to replace the InstanceManager using
            // reflection to access private fields
            ApplicationContext appContext = (ApplicationContext) getContextFieldValue((ApplicationContextFacade) context,
                    ApplicationContextFacade.class);
            return (StandardContext) getContextFieldValue(appContext, ApplicationContext.class);
        } catch (Exception e) {
            throw TomcatLogger.LOG.cannotGetStandardContext(e);
        }
    }

    private static <E> Object getContextFieldValue(E obj, Class<E> clazz) throws NoSuchFieldException, IllegalAccessException {
        Field field = SecurityActions.lookupField(clazz, CONTEXT_FIELD_NAME);
        SecurityActions.ensureAccessible(field);
        return field.get(obj);
    }

    private static InstanceManager getInstanceManager(StandardContext stdContext) {
        try {
            Method method = SecurityActions.lookupMethod(stdContext.getClass(), INSTANCE_MANAGER_GETTER_NAME);
            SecurityActions.ensureAccessible(method);
            try {
                return Reflections.cast(method.invoke(stdContext));
            } catch (Exception e) {
                TomcatLogger.LOG.errorInvokingMethod(method.getName(), stdContext, Arrays2.EMPTY_ARRAY);
            }
        } catch (NoSuchMethodException e1) {
            // Getter/setter not found
        }
        try {
            Field field = SecurityActions.lookupField(stdContext.getClass(), INSTANCE_MANAGER_FIELD_NAME);
            SecurityActions.ensureAccessible(field);
            try {
                return Reflections.cast(field.get(stdContext));
            } catch (Exception e) {
                TomcatLogger.LOG.errorReadingField(field.getName(), stdContext);
            }
        } catch (NoSuchFieldException e1) {
            // Field not found
        }
        throw TomcatLogger.LOG.neitherFieldNorGetterSetterFound(stdContext.getClass());
    }

    private static void setInstanceManager(StandardContext stdContext, InstanceManager instanceManager) {
        try {
            Method method = SecurityActions.lookupMethod(stdContext.getClass(), INSTANCE_MANAGER_SETTER_NAME,
                    InstanceManager.class);
            SecurityActions.ensureAccessible(method);
            try {
                method.invoke(stdContext, instanceManager);
                return;
            } catch (Exception e) {
                TomcatLogger.LOG.errorInvokingMethod(method.getName(), stdContext, instanceManager);
            }
        } catch (NoSuchMethodException e1) {
            // Getter/setter not found
        }
        try {
            Field field = SecurityActions.lookupField(stdContext.getClass(), INSTANCE_MANAGER_FIELD_NAME);
            SecurityActions.ensureAccessible(field);
            try {
                field.set(stdContext, instanceManager);
                return;
            } catch (Exception e) {
                TomcatLogger.LOG.errorWritingField(field.getName(), stdContext, instanceManager);
            }
        } catch (NoSuchFieldException e1) {
            // Field not found
        }
        throw TomcatLogger.LOG.neitherFieldNorGetterSetterFound(stdContext.getClass());
    }

}

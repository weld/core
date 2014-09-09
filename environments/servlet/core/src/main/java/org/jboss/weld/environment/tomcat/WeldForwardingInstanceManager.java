package org.jboss.weld.environment.tomcat;

import static org.jboss.weld.environment.servlet.util.Reflections.findDeclaredField;
import static org.jboss.weld.environment.servlet.util.Reflections.findDeclaredMethod;
import static org.jboss.weld.environment.servlet.util.Reflections.getFieldValue;
import static org.jboss.weld.environment.servlet.util.Reflections.invokeMethod;
import static org.jboss.weld.environment.servlet.util.Reflections.setFieldValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.InstanceManager;
import org.jboss.weld.environment.servlet.logging.TomcatLogger;
import org.jboss.weld.manager.api.WeldManager;

/**
 * Forwards all calls in turn to two delegates: first to InstanceManager, then
 * to WeldInstanceManager
 *
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 */
public class WeldForwardingInstanceManager extends ForwardingInstanceManager {
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
    public Object newInstance(String fqcn, ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        Object a = super.newInstance(fqcn, classLoader);
        secondProcessor.newInstance(a);
        return a;
    }

    @Override
    public Object newInstance(String fqcn) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        Object a = super.newInstance(fqcn);
        secondProcessor.newInstance(a);
        return a;
    }

    @Override
    public Object newInstance(Class<?> clazz) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException {
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
            ApplicationContext appContext = (ApplicationContext) getContextFieldValue((ApplicationContextFacade) context, ApplicationContextFacade.class);
            return (StandardContext) getContextFieldValue(appContext, ApplicationContext.class);
        } catch (Exception e) {
            throw TomcatLogger.LOG.cannotGetStandardContext(e);
        }
    }

    private static <E> Object getContextFieldValue(E obj, Class<E> clazz) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField("context");
        f.setAccessible(true);
        return f.get(obj);
    }

    public static void restoreInstanceManager(ServletContext context) {
        StandardContext stdContext = getStandardContext(context);
        InstanceManager im = getInstanceManager(stdContext);
        if (im instanceof WeldForwardingInstanceManager) {
            setInstanceManager(stdContext, ((WeldForwardingInstanceManager) im).firstProcessor);
        }
    }

    private static InstanceManager getInstanceManager(StandardContext stdContext) {

        Method method = findDeclaredMethod(stdContext.getClass(), "getInstanceManager");
        if (method != null) {
            return invokeMethod(method, InstanceManager.class, stdContext);
        }
        Field field = findDeclaredField(stdContext.getClass(), INSTANCE_MANAGER_FIELD_NAME);
        if (field != null) {
            return getFieldValue(field, stdContext, InstanceManager.class);
        }
        throw TomcatLogger.LOG.neitherFieldNorSetterFound();
    }

    private static void setInstanceManager(StandardContext stdContext, InstanceManager instanceManager) {

        Method method = findDeclaredMethod(stdContext.getClass(), "setInstanceManager", InstanceManager.class);
        if (method != null) {
            invokeMethod(method, void.class, stdContext, instanceManager);
            return;
        }
        Field field = findDeclaredField(stdContext.getClass(), INSTANCE_MANAGER_FIELD_NAME);
        if (field != null) {
            setFieldValue(field, stdContext, instanceManager);
            return;
        }
        throw TomcatLogger.LOG.neitherFieldNorSetterFound();
    }

}

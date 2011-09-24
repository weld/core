package org.jboss.weld.environment.tomcat7;

import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.InstanceManager;
import org.jboss.weld.manager.api.WeldManager;

import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.jboss.weld.environment.servlet.util.Reflections.findDeclaredField;
import static org.jboss.weld.environment.servlet.util.Reflections.findDeclaredMethod;
import static org.jboss.weld.environment.servlet.util.Reflections.getFieldValue;
import static org.jboss.weld.environment.servlet.util.Reflections.invokeMethod;
import static org.jboss.weld.environment.servlet.util.Reflections.setFieldValue;

/**
 * Forwards all calls in turn to two delegates: first to InstanceManager, then
 * to WeldInstanceManager
 *
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 */
public class WeldForwardingInstanceManager extends ForwardingInstanceManager {
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

    public static void replacInstanceManager(ServletContextEvent sce, WeldManager manager) {
        StandardContext stdContext = getStandardContext(sce);
        setInstanceManager(stdContext, createInstance(manager, stdContext));
    }

    private static WeldForwardingInstanceManager createInstance(WeldManager manager, StandardContext stdContext) {
        try {

            InstanceManager weldProcessor = new WeldInstanceManager(manager);
            return new WeldForwardingInstanceManager(getInstanceManager(stdContext), weldProcessor);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create WeldForwardingAnnotationProcessor", e);
        }
    }

    private static StandardContext getStandardContext(ServletContextEvent sce) {
        try {
            // Hack into Tomcat to replace the InstanceManager using
            // reflection to access private fields
            ApplicationContext appContext = (ApplicationContext) getContextFieldValue((ApplicationContextFacade) sce.getServletContext(), ApplicationContextFacade.class);
            return (StandardContext) getContextFieldValue(appContext, ApplicationContext.class);
        } catch (Exception e) {
            throw new RuntimeException("Cannot get StandardContext from ServletContext", e);
        }
    }

    private static <E> Object getContextFieldValue(E obj, Class<E> clazz) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField("context");
        f.setAccessible(true);
        return f.get(obj);
    }

    public static void restoreInstanceManager(ServletContextEvent sce) {
        StandardContext stdContext = getStandardContext(sce);
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
        Field field = findDeclaredField(stdContext.getClass(), "instanceManager");
        if (field != null) {
            return getFieldValue(field, stdContext, InstanceManager.class);
        }
        throw new RuntimeException("neither field nor setter found for instanceManager");
    }

    private static void setInstanceManager(StandardContext stdContext, InstanceManager instanceManager) {

        Method method = findDeclaredMethod(stdContext.getClass(), "setInstanceManager", InstanceManager.class);
        if (method != null) {
            invokeMethod(method, void.class, stdContext, instanceManager);
            return;
        }
        Field field = findDeclaredField(stdContext.getClass(), "instanceManager");
        if (field != null) {
            setFieldValue(field, stdContext, instanceManager);
            return;
        }
        throw new RuntimeException("neither field nor setter found for instanceManager");
    }

}
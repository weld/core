package org.jboss.interceptor.proxy.javassist;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javassist.util.proxy.MethodHandler;

/**
 * A wrapper for multiple Javassist method handlers.
 *
 * @author Marius Bogoevici
 */
public class CompositeHandler implements MethodHandler, Serializable {

    protected static final String OBJECT_CLASS_NAME = Object.class.getName();

    private List<MethodHandler> methodHandlers;

    public CompositeHandler(List<MethodHandler> methodHandlers) {
        this.methodHandlers = new ArrayList<MethodHandler>();
        this.methodHandlers.addAll(methodHandlers);
    }

    private static ThreadLocal<Integer> currentHandlerIndex = new ThreadLocal<Integer>();

    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        if (thisMethod.getDeclaringClass().getName().equals(OBJECT_CLASS_NAME))
            return proceed.invoke(self);
        boolean isOuter = false;
        if (currentHandlerIndex.get() == null) {
            isOuter = true;
            currentHandlerIndex.set(0);
        } else {
            currentHandlerIndex.set(currentHandlerIndex.get() + 1);
        }
        try {
            if (currentHandlerIndex.get() < methodHandlers.size()) {
                return methodHandlers.get(currentHandlerIndex.get()).invoke(self, thisMethod, proceed, args);
            } else {
                if (proceed != null)
                    return proceed.invoke(self, args);
            }
        } finally {
            if (isOuter) {
                currentHandlerIndex.set(null);
            }
        }
        return null;
    }
}

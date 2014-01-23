package org.jboss.weld.interceptor.proxy;

import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;

import org.jboss.weld.interceptor.spi.model.InterceptionType;

/**
* @author Marius Bogoevici
*/
public class SimpleMethodInvocation implements InterceptorMethodInvocation {

    final Object instance;

    final Method method;

    private boolean targetClass;

    private InterceptionType interceptionType;

    SimpleMethodInvocation(Object instance, Method method, boolean targetClass, InterceptionType interceptionType) {
        this.instance = instance;
        this.method = method;
        this.targetClass = targetClass;
        this.interceptionType = interceptionType;
    }

    @Override
    public Object invoke(InvocationContext invocationContext) throws Exception {
        if (invocationContext != null) {
            return method.invoke(instance, invocationContext);
        }
        else {
            return method.invoke(instance);
        }
    }

    @Override
    public boolean expectsInvocationContext() {
        return !targetClass || !interceptionType.isLifecycleCallback();
    }
}

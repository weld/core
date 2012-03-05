package org.jboss.weld.interceptor.proxy;

import javax.interceptor.InvocationContext;

import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionType;

/**
* @author Marius Bogoevici
*/
public class SimpleMethodInvocation<T> implements InterceptorMethodInvocation {

    final T instance;

    final MethodMetadata method;

    private boolean targetClass;

    private InterceptionType interceptionType;

    SimpleMethodInvocation(T instance, MethodMetadata method, boolean targetClass, InterceptionType interceptionType) {
        this.instance = instance;
        this.method = method;
        this.targetClass = targetClass;
        this.interceptionType = interceptionType;
    }

    public Object invoke(InvocationContext invocationContext) throws Exception {
        if (invocationContext != null)
            return method.getJavaMethod().invoke(instance, invocationContext);
        else
            return method.getJavaMethod().invoke(instance);
    }

    public MethodMetadata getMethod() {
        return method;
    }

    public boolean expectsInvocationContext() {
        return !targetClass || !interceptionType.isLifecycleCallback();
    }
}

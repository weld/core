package org.jboss.weld.interceptor.proxy;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;


/**
 * @author Marius Bogoevici
 */
public class CustomInterceptorInvocation<T> implements InterceptorInvocation {

    private final Interceptor<T> interceptorBeanInstance;

    private final T interceptorInstance;

    private final InterceptionType interceptionType;

    public CustomInterceptorInvocation(Interceptor<T> interceptorBeanInstance, T interceptorInstance, InterceptionType interceptionType) {
        this.interceptorBeanInstance = interceptorBeanInstance;
        this.interceptorInstance = interceptorInstance;
        this.interceptionType = interceptionType;
    }

    public Collection<InterceptorMethodInvocation> getInterceptorMethodInvocations() {
        return Collections.<InterceptorMethodInvocation>singleton(new CustomInterceptorMethodInvocation());
    }

    private class CustomInterceptorMethodInvocation implements InterceptorMethodInvocation {
        public Object invoke(InvocationContext invocationContext) throws Exception {
            return interceptorBeanInstance.intercept(interceptionType, interceptorInstance, invocationContext);
        }

        public boolean expectsInvocationContext() {
            return true;
        }
    }

}

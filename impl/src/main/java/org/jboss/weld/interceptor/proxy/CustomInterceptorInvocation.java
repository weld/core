package org.jboss.weld.interceptor.proxy;

import java.util.Collections;
import java.util.List;

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

    @Override
    public List<InterceptorMethodInvocation> getInterceptorMethodInvocations() {
        return Collections.<InterceptorMethodInvocation>singletonList(new CustomInterceptorMethodInvocation());
    }

    private class CustomInterceptorMethodInvocation implements InterceptorMethodInvocation {
        @Override
        public Object invoke(InvocationContext invocationContext) throws Exception {
            return interceptorBeanInstance.intercept(interceptionType, interceptorInstance, invocationContext);
        }

        @Override
        public boolean expectsInvocationContext() {
            return true;
        }
    }

}

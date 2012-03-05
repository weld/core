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

    private final Interceptor<?> interceptorBeanInstance;

    private final T interceptorInstance;

    private final InterceptionType interceptionType;

    public CustomInterceptorInvocation(Interceptor<?> interceptorBeanInstance, T interceptorInstance, InterceptionType interceptionType) {
        this.interceptorBeanInstance = interceptorBeanInstance;
        this.interceptorInstance = interceptorInstance;
        this.interceptionType = interceptionType;
    }

    private static final Method intercept;

    static {
        try {
            intercept = Interceptor.class.getDeclaredMethod("intercept", javax.enterprise.inject.spi.InterceptionType.class, Object.class, InvocationContext.class );
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public Collection<InterceptorMethodInvocation> getInterceptorMethodInvocations() {
        return Collections.<InterceptorMethodInvocation>singleton(new CustomInterceptorMethodInvocation());
    }

    private class CustomInterceptorMethodInvocation implements InterceptorMethodInvocation {
        public Object invoke(InvocationContext invocationContext) throws Exception {
            return intercept.invoke(interceptorBeanInstance, interceptionType, interceptorInstance, invocationContext);
        }

        public boolean expectsInvocationContext() {
            return true;
        }
    }

}

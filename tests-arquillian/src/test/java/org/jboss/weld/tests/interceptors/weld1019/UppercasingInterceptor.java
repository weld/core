package org.jboss.weld.tests.interceptors.weld1019;

import java.io.Serializable;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 *
 */
@Interceptor
@UpperCased
public class UppercasingInterceptor implements Serializable {
    private static final long serialVersionUID = 7685137612002720995L;

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        Object value = ctx.proceed();
        if (value instanceof String) {
            return ((String) value).toUpperCase();
        } else {
            throw new RuntimeException("UppercasingInterceptor can only intercept methods that return String");
        }
    }
}

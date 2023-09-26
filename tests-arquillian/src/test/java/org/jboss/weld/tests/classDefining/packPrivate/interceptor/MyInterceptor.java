package org.jboss.weld.tests.classDefining.packPrivate.interceptor;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@SomeBinding
@Priority(Interceptor.Priority.APPLICATION)
public class MyInterceptor {

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        return this.getClass().getSimpleName() + context.proceed();
    }
}

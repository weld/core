package org.jboss.weld.tests.contexts.creational;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@PreDestroyBinding
@Interceptor
@Priority(2)
public class MyPreDestroyInterceptor {

    public static boolean preDestroyCalled = false;

    @PreDestroy
    public void preDestroy(InvocationContext invocationContext) throws Exception {
        preDestroyCalled = true;
        invocationContext.proceed();
    }
}

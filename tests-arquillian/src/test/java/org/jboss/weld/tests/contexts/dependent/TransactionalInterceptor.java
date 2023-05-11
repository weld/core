package org.jboss.weld.tests.contexts.dependent;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Dependent
@Transactional
@Interceptor
@Priority(1)
public class TransactionalInterceptor {
    public static boolean intercepted = false;

    @Inject
    TransactionalInterceptorDependency dependency;

    @AroundInvoke
    public Object alwaysReturnThis(InvocationContext ctx) throws Exception {
        intercepted = true;
        return ctx.proceed();
    }
}

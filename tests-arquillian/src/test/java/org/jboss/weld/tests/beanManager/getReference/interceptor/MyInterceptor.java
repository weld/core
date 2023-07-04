package org.jboss.weld.tests.beanManager.getReference.interceptor;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Intercepted;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@Priority(1)
@MyBinding
public class MyInterceptor {

    @Inject
    @Intercepted
    Bean<?> bean;

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception {
        return ic.proceed();
    }
}

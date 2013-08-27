package org.jboss.weld.tests.interceptors.nondependent;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 *
 */
@RequestScoped // <-- Any scope besides @Dependent causes StackOverflow
@Interceptor
@SomeBinding
public class NonDependentInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @AroundInvoke
    public Object intercept(final InvocationContext context) throws Exception {
        return context.proceed();
    }
}

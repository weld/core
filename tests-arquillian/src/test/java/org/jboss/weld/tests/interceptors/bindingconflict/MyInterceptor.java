package org.jboss.weld.tests.interceptors.bindingconflict;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@MyBinding @Interceptor
public class MyInterceptor {

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext invocation) throws Exception {
        return invocation.proceed();
    }

}

package org.jboss.weld.tests.interceptors.jaxws;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@TestInterceptor
@Interceptor
public class TestInterceptorImpl
{
    public static Integer counter = 0;

    @AroundInvoke
    public Object intercept(InvocationContext ic) throws Exception
    {
        counter++;
        return ic.proceed();
    }
}
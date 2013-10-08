package org.jboss.weld.tests.interceptors.extension.annotation;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public class BooInterceptor {

    @AroundInvoke
    public Object interceptFoo(InvocationContext ctx) throws Exception {
        if (ctx.getMethod().getName().equals("simpleMethod")) {
            return "intercepted";
        }
        return ctx.proceed();
}

}
package org.jboss.weld.tests.ejb.duplicatenames;

import java.util.ArrayList;
import java.util.List;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Logged
public class LogInterceptor {

    public static final List QUEUE = new ArrayList<String>(2);

    @AroundInvoke
    public Object execute(InvocationContext ctx) throws Exception {
        Object result = ctx.proceed();
        QUEUE.add(result.toString());
        return result;
    }
}

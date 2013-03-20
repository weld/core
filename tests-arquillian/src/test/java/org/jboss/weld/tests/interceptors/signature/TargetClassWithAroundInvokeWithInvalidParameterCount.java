package org.jboss.weld.tests.interceptors.signature;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class TargetClassWithAroundInvokeWithInvalidParameterCount {
    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx, String anotherParameter) throws Exception {
        return ctx.proceed();
    }

    public String foo() {
        return "foo";
    }
}

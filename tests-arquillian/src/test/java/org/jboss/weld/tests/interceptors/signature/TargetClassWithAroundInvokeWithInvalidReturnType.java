package org.jboss.weld.tests.interceptors.signature;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class TargetClassWithAroundInvokeWithInvalidReturnType {
    @AroundInvoke
    public void aroundInvoke(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }

    public String foo() {
        return "foo";
    }
}

package org.jboss.weld.tests.interceptors.signature;

import jakarta.enterprise.context.Dependent;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Dependent
public class TargetClassWithAroundInvokeWithInvalidReturnType {
    @AroundInvoke
    public void aroundInvoke(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }

    public String foo() {
        return "foo";
    }
}

package org.jboss.weld.tests.interceptors.signature;

import jakarta.enterprise.context.Dependent;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Dependent
public class TargetClassWithAroundInvokeWithInvalidParameterCount {
    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx, String anotherParameter) throws Exception {
        return ctx.proceed();
    }

    public String foo() {
        return "foo";
    }
}

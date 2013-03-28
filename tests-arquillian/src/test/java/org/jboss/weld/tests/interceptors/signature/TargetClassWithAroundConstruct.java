package org.jboss.weld.tests.interceptors.signature;

import javax.interceptor.AroundConstruct;
import javax.interceptor.InvocationContext;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class TargetClassWithAroundConstruct {
    @AroundConstruct
    public void aroundConstruct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }

    public String foo() {
        return "foo";
    }
}

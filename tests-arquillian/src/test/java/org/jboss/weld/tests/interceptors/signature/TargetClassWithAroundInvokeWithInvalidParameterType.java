package org.jboss.weld.tests.interceptors.signature;

import jakarta.enterprise.context.Dependent;
import jakarta.interceptor.AroundInvoke;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
@Dependent
public class TargetClassWithAroundInvokeWithInvalidParameterType {
    @AroundInvoke
    public Object aroundInvoke(String notInvocationContext) throws Exception {
        return null;
    }

    public String foo() {
        return "foo";
    }
}

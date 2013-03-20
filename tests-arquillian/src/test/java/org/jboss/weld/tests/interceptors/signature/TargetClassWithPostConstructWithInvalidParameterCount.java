package org.jboss.weld.tests.interceptors.signature;

import javax.annotation.PostConstruct;
import javax.interceptor.InvocationContext;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class TargetClassWithPostConstructWithInvalidParameterCount {
    @PostConstruct
    public void postConstructWithInvalidParameterCount(InvocationContext invocationContext) {
    }

    public String foo() {
        return "foo";
    }
}

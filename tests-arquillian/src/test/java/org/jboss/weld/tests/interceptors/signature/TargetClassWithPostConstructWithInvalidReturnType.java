package org.jboss.weld.tests.interceptors.signature;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
@Dependent
public class TargetClassWithPostConstructWithInvalidReturnType {

    @PostConstruct
    public Object postConstructWithInvalidReturnType() {
        return null;
    }

    public String foo() {
        return "foo";
    }
}

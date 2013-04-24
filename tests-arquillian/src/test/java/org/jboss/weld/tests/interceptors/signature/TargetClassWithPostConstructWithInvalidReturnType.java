package org.jboss.weld.tests.interceptors.signature;

import javax.annotation.PostConstruct;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class TargetClassWithPostConstructWithInvalidReturnType {

    @PostConstruct
    public Object postConstructWithInvalidReturnType() {
        return null;
    }

    public String foo() {
        return "foo";
    }
}

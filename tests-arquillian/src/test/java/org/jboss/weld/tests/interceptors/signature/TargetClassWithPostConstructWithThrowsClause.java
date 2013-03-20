package org.jboss.weld.tests.interceptors.signature;

import javax.annotation.PostConstruct;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class TargetClassWithPostConstructWithThrowsClause {
    public boolean postConstructInvoked;

    @PostConstruct
    public void postConstruct() throws Exception {
        postConstructInvoked = true;
    }

    public String foo() {
        return "foo";
    }
}

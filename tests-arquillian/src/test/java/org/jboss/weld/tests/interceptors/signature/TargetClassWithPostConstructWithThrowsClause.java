package org.jboss.weld.tests.interceptors.signature;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Dependent
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

package org.jboss.weld.tests.invokable.lookup;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@MyQualifier1("myBean")
public class InvokableBean {

    public String instanceLookup() {
        return InvokableBean.class.getSimpleName();
    }

    public String correctLookup(@MyQualifier1("noMatter") @MyQualifier4("binding") String a, @MyQualifier2 String b) {
        return a + b;
    }

    public String lookupWithRegisteredQualifier(@ToBeQualifier String a) {
        return a;
    }
}

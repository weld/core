package org.jboss.weld.tests.invokable.lookup;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.invoke.Invokable;

@ApplicationScoped
@MyQualifier1("myBean")
@Invokable
public class InvokableBean {

    public String instanceLookup() {
        return InvokableBean.class.getSimpleName();
    }

    // there are two producers providing a bean for the first argument
    public String ambiguousLookup(@MyQualifier5 String a) {
        return a;
    }

    // there is no bean with @MyQualifier3
    public String unsatisfiedLookup(@MyQualifier3 String a) {
        return a;
    }

    public String correctLookup(@MyQualifier1("noMatter") @MyQualifier4("binding") String a, @MyQualifier2 String b) {
        return a + b;
    }

    public String lookupWithRegisteredQualifier(@ToBeQualifier String a) {
        return a;
    }
}

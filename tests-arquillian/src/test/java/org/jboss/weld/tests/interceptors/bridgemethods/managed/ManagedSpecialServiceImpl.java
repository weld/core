package org.jboss.weld.tests.interceptors.bridgemethods.managed;

import jakarta.enterprise.context.Dependent;
import org.jboss.weld.tests.interceptors.bridgemethods.common.SomeInterceptorBinding;
import org.jboss.weld.tests.interceptors.bridgemethods.common.SpecialService;


/**
 *
 */
@SomeInterceptorBinding
@Dependent
public class ManagedSpecialServiceImpl implements SpecialService {

    public void doSomething(String param) {
    }

    public String returnSomething() {
        return "";
    }
}

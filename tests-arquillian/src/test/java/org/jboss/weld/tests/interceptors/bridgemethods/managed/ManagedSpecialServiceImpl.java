package org.jboss.weld.tests.interceptors.bridgemethods.managed;

import org.jboss.weld.tests.interceptors.bridgemethods.common.BaseServiceImpl;
import org.jboss.weld.tests.interceptors.bridgemethods.common.SomeInterceptorBinding;
import org.jboss.weld.tests.interceptors.bridgemethods.common.SpecialService;


/**
 *
 */
@SomeInterceptorBinding
public class ManagedSpecialServiceImpl extends BaseServiceImpl<String> implements SpecialService {

    public void doSomething(String param) {
    }

    public String returnSomething() {
        return "";
    }
}

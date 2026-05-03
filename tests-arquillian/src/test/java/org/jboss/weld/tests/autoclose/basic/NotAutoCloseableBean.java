package org.jboss.weld.tests.autoclose.basic;

import jakarta.enterprise.context.AutoClose;
import jakarta.enterprise.context.Dependent;

@Dependent
@AutoClose
public class NotAutoCloseableBean {
    public String ping() {
        return "open";
    }
}

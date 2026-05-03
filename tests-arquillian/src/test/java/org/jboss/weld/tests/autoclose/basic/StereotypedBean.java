package org.jboss.weld.tests.autoclose.basic;

import jakarta.enterprise.context.Dependent;

import org.jboss.weld.test.util.ActionSequence;

@Dependent
@AutoCloseStereotype
public class StereotypedBean implements AutoCloseable {
    public String ping() {
        return "open";
    }

    @Override
    public void close() {
        ActionSequence.addAction("StereotypedBean.close");
    }
}

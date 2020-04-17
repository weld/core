package org.jboss.weld.tests.el.resolver;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
public class OrderBean {

    @Inject
    public void init() {
        throw new OrderException();
    }

    public Long getOrderId() {
        return 1L;
    }
}

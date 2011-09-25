package org.jboss.weld.tests.el.resolver;

import javax.inject.Inject;
import javax.inject.Named;

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

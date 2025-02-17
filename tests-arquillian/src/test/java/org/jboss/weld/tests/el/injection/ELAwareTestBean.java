package org.jboss.weld.tests.el.injection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named("testbean")
@ApplicationScoped
public class ELAwareTestBean {

    public String getValue() {
        return "hello";
    }
}

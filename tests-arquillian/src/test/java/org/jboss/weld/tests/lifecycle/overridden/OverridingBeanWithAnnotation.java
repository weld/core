package org.jboss.weld.tests.lifecycle.overridden;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 *
 */
public class OverridingBeanWithAnnotation extends AbstractBean {

    @PostConstruct
    public void postConstruct() {
        super.postConstruct();
    }

    @PreDestroy
    public void preDestroy() {
        super.preDestroy();
    }
}

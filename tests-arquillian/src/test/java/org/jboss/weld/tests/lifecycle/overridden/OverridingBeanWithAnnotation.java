package org.jboss.weld.tests.lifecycle.overridden;

import javax.annotation.PostConstruct;

/**
 *
 */
public class OverridingBeanWithAnnotation extends AbstractBean {

    @PostConstruct
    public void postConstruct() {
        super.postConstruct();
    }

}

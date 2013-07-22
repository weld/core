package org.jboss.weld.tests.lifecycle.overridden;

/**
 *
 */
public class OverridingBeanWithoutAnnotation extends AbstractBean {

    public void postConstruct() {
        super.postConstruct();
    }

    public void preDestroy() {
        super.preDestroy();
    }
}

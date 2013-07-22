package org.jboss.weld.tests.lifecycle.overridden;

import javax.annotation.PostConstruct;

/**
 *
 */
public abstract class AbstractBean {

    protected int postConstructInvocationCount;

    @PostConstruct
    public void postConstruct() {
        postConstructInvocationCount++;
    }

    public int getPostConstructInvocationCount() {
        return postConstructInvocationCount;
    }
}

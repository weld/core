package org.jboss.weld.tests.lifecycle.overridden;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 *
 */
public abstract class AbstractBean {

    protected int postConstructInvocationCount;
    protected int preDestroyInvocationCount;

    @PostConstruct
    public void postConstruct() {
        postConstructInvocationCount++;
    }

    @PreDestroy
    public void preDestroy() {
        preDestroyInvocationCount++;
    }

    public int getPostConstructInvocationCount() {
        return postConstructInvocationCount;
    }

    public int getPreDestroyInvocationCount() {
        return preDestroyInvocationCount;
    }
}

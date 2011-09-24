package org.jboss.weld.tests.contexts.conversation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@ApplicationScoped
@Named
public class Hurricane {

    private boolean preDestroyCalledOnCloud;

    public boolean isPreDestroyCalledOnCloud() {
        return preDestroyCalledOnCloud;
    }

    public void setPreDestroyCalledOnCloud(boolean preDestroyCalledOnCloud) {
        this.preDestroyCalledOnCloud = preDestroyCalledOnCloud;
    }

}

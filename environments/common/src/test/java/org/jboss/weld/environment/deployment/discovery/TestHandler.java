package org.jboss.weld.environment.deployment.discovery;

import javax.annotation.Priority;

@Priority(10)
class TestHandler implements BeanArchiveHandler {

    @Override
    public BeanArchiveBuilder handle(String beanArchiveReference) {
        return null;
    }

}
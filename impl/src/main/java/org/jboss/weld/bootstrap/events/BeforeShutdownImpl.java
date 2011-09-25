/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.bootstrap.events;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.manager.BeanManagerImpl;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.BeforeShutdown;
import java.util.Map;

import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.util.reflection.Reflections.EMPTY_TYPES;

/**
 * @author pmuir
 */
public class BeforeShutdownImpl extends AbstractContainerEvent implements BeforeShutdown {

    private static final Logger log = loggerFactory().getLogger(BOOTSTRAP);

    public static void fire(BeanManagerImpl beanManager, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments) {
        if (beanDeployments == null) {
            // Shutdown may have been called with an early-failure, before beanDeployments is built
            new BeforeShutdownImpl(beanManager).fire();
        } else {
            new BeforeShutdownImpl(beanManager).fire(beanDeployments);
        }
    }

    public BeforeShutdownImpl(BeanManagerImpl beanManager) {
        super(beanManager, BeforeShutdown.class, EMPTY_TYPES);
    }

    @Override
    protected void fire(Map<BeanDeploymentArchive, BeanDeployment> beanDeployments) {
        super.fire(beanDeployments);
        if (!getErrors().isEmpty()) {
            log.error("Exception(s) thrown during observer of BeforeShutdown");
            for (Throwable t : getErrors()) {
                log.error("", t);
            }
        }
    }

}

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

import static org.jboss.weld.util.reflection.Reflections.EMPTY_TYPES;

import javax.enterprise.inject.spi.BeforeShutdown;

import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author pmuir
 */
public class BeforeShutdownImpl extends AbstractContainerEvent implements BeforeShutdown {

    public static void fire(BeanManagerImpl beanManager) {
        new BeforeShutdownImpl(beanManager).fire();
    }

    public BeforeShutdownImpl(BeanManagerImpl beanManager) {
        super(beanManager, BeforeShutdown.class, EMPTY_TYPES);
    }

    @Override
    public void fire() {
        super.fire();
        if (!getErrors().isEmpty()) {
            BootstrapLogger.LOG.exceptionThrownDuringBeforeShutdownObserver();
            for (Throwable t : getErrors()) {
                BootstrapLogger.LOG.error("", t);
            }
        }
    }

}

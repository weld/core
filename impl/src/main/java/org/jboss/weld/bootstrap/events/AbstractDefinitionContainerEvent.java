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

import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Preconditions;

import java.lang.reflect.Type;

/**
 * @author pmuir
 */
public abstract class AbstractDefinitionContainerEvent extends AbstractContainerEvent {

    protected AbstractDefinitionContainerEvent(BeanManagerImpl beanManager, Type rawType, Type[] actualTypeArguments) {
        super(beanManager, rawType, actualTypeArguments);
    }

    public void addDefinitionError(Throwable t) {
        Preconditions.checkArgumentNotNull(t, "Throwable t");
        checkWithinObserverNotification();
        getErrors().add(t);
        BootstrapLogger.LOG.addDefinitionErrorCalled(getReceiver(), t);
    }

    @Override
    public void fire() {
        super.fire();
        if (!getErrors().isEmpty()) {
            if (getErrors().size() == 1 && getErrors().get(0) instanceof DeploymentException) {
                // if the throwable was deployment exception, rethrow that
                    throw (DeploymentException) getErrors().get(0);
            } else {
                throw new DefinitionException(getErrors());
            }
        }
    }
}

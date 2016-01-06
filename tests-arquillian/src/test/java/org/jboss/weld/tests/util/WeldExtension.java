/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.tests.util;

import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.weld.tests.CategoryArchiveAppender;

/**
 * Arquillian loadable extension.
 *
 * @author Aslak Knutsen
 * @author Stuart Douglas
 * @author Martin Kouba
 */
public class WeldExtension implements LoadableExtension {

    // TODO the managed container class did not change so far, but will likely change soon
    private static final String MANAGED_CONTAINER_CLASS = "org.jboss.as.arquillian.container.managed.ManagedDeployableContainer";

    public void register(ExtensionBuilder builder) {
        builder.service(AuxiliaryArchiveAppender.class, CategoryArchiveAppender.class);
        builder.service(DeploymentExceptionTransformer.class, WeldExceptionTransformer.class);
        if(Validate.classExists(MANAGED_CONTAINER_CLASS)){
            builder.observer(JBossASResourceManager.class);
        }
    }

}

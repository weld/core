/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tck.wildfly8;

import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.as.arquillian.container.ExceptionTransformer;

/**
 * @author Martin Kouba
 */
public class WildFly8Extension implements LoadableExtension {

    private static final String MANAGED_CONTAINER_CLASS = "org.jboss.as.arquillian.container.managed.ManagedDeployableContainer";
    private static final String REMOTE_CONTAINER_CLASS = "org.jboss.as.arquillian.container.remote.RemoteDeployableContainer";

    public void register(ExtensionBuilder builder) {

        if (Validate.classExists(MANAGED_CONTAINER_CLASS) || Validate.classExists(REMOTE_CONTAINER_CLASS)) {

            // Override the default NOOP exception transformer
            builder.override(DeploymentExceptionTransformer.class, ExceptionTransformer.class,
                    WildFly8DeploymentExceptionTransformer.class);

            if (Validate.classExists(MANAGED_CONTAINER_CLASS)) {
                // Observe container start and check EE resources
                builder.observer(WildFly8EEResourceManager.class);
            }
        }
    }

}

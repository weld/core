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
package org.jboss.weld.tck.as7;

import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.as.arquillian.container.ExceptionTransformer;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.util.reflection.Reflections;

/**
 * This arquillian extention is TEMPORARY WORKAROUND. It overrides the default NOOP exception transformer for JBoss managed
 * container.
 *
 * See AS7-1197 for more details.
 *
 * @author Martin Kouba
 */
public class JBossAS7DeploymentExceptionTransformerExtension implements LoadableExtension {

    private static final String JBOSSAS7_TEST_CLASS = "org.jboss.as.arquillian.container.managed.ManagedDeployableContainer";

    public void register(ExtensionBuilder builder) {

        if (Reflections.isClassLoadable(JBOSSAS7_TEST_CLASS, new ClassLoaderResourceLoader(this.getClass().getClassLoader()))) {
            // Override the default NOOP exception transformer
            builder.override(DeploymentExceptionTransformer.class, ExceptionTransformer.class,
                    JBossAS7DeploymentExceptionTransformer.class);
        }
    }

}

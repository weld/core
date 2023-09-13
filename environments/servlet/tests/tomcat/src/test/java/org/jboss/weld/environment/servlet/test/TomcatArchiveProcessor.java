/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.servlet.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.util.Deployments;
import org.jboss.weld.environment.servlet.test.util.TomcatDeployments;

/**
 * This processor adds Tomcat-specific resources to the test archive.
 *
 * <p>
 * Note that if a test deployment is not testable, i.e. {@link Deployment#testable()} is set to <code>false</code>, the
 * processor is not invoked.
 * </p>
 *
 * @author Martin Kouba
 */
public class TomcatArchiveProcessor implements ApplicationArchiveProcessor {

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (Deployments.isProcessorSkipped(applicationArchive)) {
            return;
        }
        if (applicationArchive instanceof WebArchive) {
            TomcatDeployments.apply((WebArchive) applicationArchive);
        } else {
            throw new IllegalStateException("Only web archives are supported");
        }
    }

}

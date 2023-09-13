/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.decorators.builtin.validation;

import jakarta.enterprise.inject.spi.DeploymentException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Validates that if a decorator which has a non-passivation capable dependency decorates a built-in bean, Weld detects this as
 * a {@link DeploymentException}.
 *
 * @author Jozef Hartinger
 * @see WELD-1273
 *
 */
@RunWith(Arquillian.class)
public class BuiltInBeanPassivationCapabilityValidationTest {

    @Deployment
    @ShouldThrowException(DeploymentException.class)
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(BuiltInBeanPassivationCapabilityValidationTest.class))
                .decorate(ConversationDecorator.class)
                .addPackage(BuiltInBeanPassivationCapabilityValidationTest.class.getPackage());
    }

    @Test
    public void testDeploymentFailsWithNonPassivationCapableBean() {
        // should throw deployment exception
    }
}

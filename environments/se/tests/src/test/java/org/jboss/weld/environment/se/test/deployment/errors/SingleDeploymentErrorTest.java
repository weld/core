/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.deployment.errors;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class SingleDeploymentErrorTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class).addPackage(SingleDeploymentErrorTest.class.getPackage())).build();
    }

    @Test
    public void testErrors() {
        try {
            new Weld().addExtension(new TestExtension()).initialize();
            Assert.fail("The deployment should have failed!");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof DeploymentException);
            Assert.assertEquals(0, e.getSuppressed().length);
            Assert.assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }

    static class TestExtension implements Extension {

        void afterDeploymentValidation(@Observes AfterDeploymentValidation event) {
            event.addDeploymentProblem(new IllegalStateException());
        }

    }

}
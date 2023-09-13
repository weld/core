/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.named.ambiguous.case1;

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
 * Verifies CDI 1.2 5.3.1:
 * "If the bean name of one bean is of the form x.y, where y is a valid bean name, and x is the bean
 * name of the other bean, the container automatically detects the problem and treats it as a deployment problem."
 *
 * @see WELD-1842
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class AmbiguousName1Test {

    @Deployment
    @ShouldThrowException(DeploymentException.class)
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(AmbiguousName1Test.class))
                .addPackage(AmbiguousName1Test.class.getPackage());
    }

    @Test
    public void testDeploymentWithAmbiguousBeanNames() {
        // should throw deployment exception
    }
}

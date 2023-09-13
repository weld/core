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

package org.jboss.weld.tests.enterprise.weld1234;

import static org.junit.Assert.fail;

import jakarta.ejb.NoSuchEJBException;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class Weld1234Test {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(Weld1234Test.class))
                .addPackage(Weld1234Test.class.getPackage());
    }

    @Inject
    private ExceptionGenerator exceptionGenerator;

    @Test
    public void testRemovedEjbToString() {
        try {
            exceptionGenerator.doIllegalStateException();
        } catch (Exception e) {
        }

        // Verify that toString() still works
        exceptionGenerator.toString();

        try {
            // check if other methods throw NoSuchEJBException
            exceptionGenerator.doSomethingElse();
            fail("Expected NoSuchEJBException");
        } catch (NoSuchEJBException e) {
        }
    }
}

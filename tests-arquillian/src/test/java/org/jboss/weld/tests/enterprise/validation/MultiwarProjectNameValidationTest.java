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
package org.jboss.weld.tests.enterprise.validation;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Verifies that ambiguous name validation does not cross visibility boundaries.
 *
 * @see WELD-1065
 *
 * @author Jozef Hartinger
 *
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class MultiwarProjectNameValidationTest {

    @Deployment(testable = false)
    public static Archive<?> getDeployment() {
        WebArchive war1 = ShrinkWrap.create(WebArchive.class).addClass(Alpha.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        WebArchive war2 = ShrinkWrap.create(WebArchive.class).addClass(Bravo.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap
                .create(EnterpriseArchive.class,
                        Utils.getDeploymentNameAsHash(MultiwarProjectNameValidationTest.class, Utils.ARCHIVE_TYPE.EAR))
                .addAsModules(war1, war2);
    }

    @Test
    public void testMultiWarDeploymentWithNoNameConflicts() {
        // no deployment exception should occur, since the ambiguous name is distributed over isolated sub-deployments
    }
}

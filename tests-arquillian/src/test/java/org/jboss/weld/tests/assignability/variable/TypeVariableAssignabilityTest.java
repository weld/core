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
package org.jboss.weld.tests.assignability.variable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TypeVariableAssignabilityTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(TypeVariableAssignabilityTest.class))
                .addPackage(TypeVariableAssignabilityTest.class.getPackage());
    }

    @Inject
    private InjectedBean<RuntimeException> injectedBean;

    @Test
    public void testTypeVariableAssignabilityRules() {
        assertNotNull(injectedBean.getFoo());
        assertEquals("AssignableFoo", injectedBean.getFoo().getName());
    }

    @Test
    public void testTypeVariableAssignabilityRules2() {
        assertNotNull(injectedBean.getNumberFoo());
        assertEquals("NumberFoo", injectedBean.getNumberFoo().getName());
    }
}

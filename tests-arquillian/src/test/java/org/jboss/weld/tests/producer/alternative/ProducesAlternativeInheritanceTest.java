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
package org.jboss.weld.tests.producer.alternative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ProducesAlternativeInheritanceTest {

    @Inject
    Instance<Foo> foo;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProducesAlternativeInheritanceTest.class))
                .addPackage(ProducesAlternativeInheritanceTest.class.getPackage()).addClass(Utils.class);
    }

    @Test
    public void testProducerScopeIsNotInherited() {
        Foo foo1 = foo.get();
        Foo foo2 = foo.get();
        assertEquals(AlternativeBar.NAME, foo1.getName());
        assertEquals(AlternativeBar.NAME, foo2.getName());
        // We should get two distinct @Dependent instances
        assertNotEquals(foo1, foo2);
    }

}

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.container.instance;

import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.spi.CDI;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class ContainerInstanceTest {

    @Deployment
    public static JavaArchive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ContainerInstanceTest.class))
                .addClasses(Bar.class, Foo.class);
    }

    @Test
    public void testDependentInstanceDestroy(Bar bar) {
        Foo.DESTROYED.set(false);
        // We have to use the same reference because Weld CDIProvider does not always cache CDI instances
        CDI<Object> cdi = bar.getContainer();
        Foo foo = cdi.select(Foo.class).get();
        cdi.destroy(foo);
        // We should be able to destroy a dependent bean instance obtained by CDI
        assertTrue(Foo.DESTROYED.get());
    }

}

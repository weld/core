/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.decorators.ejb;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(Integration.class)
public class EjbDecoratorTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EjbDecoratorTest.class))
                .decorate(Decorator1.class, Decorator2.class)
                .addPackage(EjbDecoratorTest.class.getPackage()).addClass(ActionSequence.class);
    }

    @Test
    public void testDependentStatefulSessionBeanDecorator(@Foo Vehicle sessionBean) {
        ActionSequence.reset();

        sessionBean.start();

        testDecoratorsInvoked(DependentStatefulCar.class);
    }

    @Test
    public void testNormalScopedStatefulSessionBeanDecorator(@Bar Vehicle sessionBean) {
        ActionSequence.reset();

        sessionBean.start();

        testDecoratorsInvoked(NormalScopedStatefulCar.class);
    }

    @Test
    public void testStatelessSessionBeanDecorator(@Baz Vehicle sessionBean) {
        ActionSequence.reset();

        sessionBean.start();

        testDecoratorsInvoked(StatelessCar.class);
    }

    @Test
    public void testSingletonDecorator(@Qux Vehicle sessionBean) {
        ActionSequence.reset();

        sessionBean.start();

        testDecoratorsInvoked(SingletonCar.class);
    }

    private void testDecoratorsInvoked(Class<?> expectedBeanClass) {
        List<String> sequence = ActionSequence.getSequenceData();
        assertEquals(5, sequence.size());
        assertEquals(Decorator1.class.getSimpleName(), sequence.get(0));
        assertEquals(Decorator2.class.getSimpleName(), sequence.get(1));
        assertEquals(expectedBeanClass.getSimpleName(), sequence.get(2));
        assertEquals(Decorator2.class.getSimpleName(), sequence.get(3));
        assertEquals(Decorator1.class.getSimpleName(), sequence.get(4));
    }

}

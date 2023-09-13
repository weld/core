/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.producer.field;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ParameterizedProducerTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ParameterizedProducerTest.class))
                .addPackage(ParameterizedProducerTest.class.getPackage());
    }

    @Test
    public void testParameterizedListInjection(Target target, ParameterizedListInjection item) {
        List<String> strings = target.getStringList();
        Assert.assertEquals(2, strings.size());

        Assert.assertEquals(2, item.getFieldInjection().size());
        Assert.assertEquals(2, item.getValue().size());
        Assert.assertEquals(2, item.getSetterInjection().size());

    }

    @Test
    public void testParameterizedCollectionInjection(Target target, ParameterizedCollectionInjection item) {
        Collection<String> strings = target.getStrings();
        Assert.assertEquals(2, strings.size());

        Assert.assertEquals(2, item.getFieldInjection().size());
        Assert.assertEquals(2, item.getValue().size());
        Assert.assertEquals(2, item.getSetterInjection().size());
    }

    @Test
    public void testIntegerCollectionInjection(Target target, IntegerCollectionInjection item) {
        Collection<Integer> integers = target.getIntegers();
        Assert.assertEquals(4, integers.size());

        Assert.assertEquals(4, item.getFieldInjection().size());
        Assert.assertEquals(4, item.getValue().size());
        Assert.assertEquals(4, item.getSetterInjection().size());

    }

    @Test
    public void testInstanceList(ListInstance listInstance) {
        Assert.assertTrue(listInstance.get().isAmbiguous());
    }

    @Test
    public void testTypeParameterInstance(ListStringInstance listInstance) {
        Assert.assertEquals(2, listInstance.get().size());
    }
}

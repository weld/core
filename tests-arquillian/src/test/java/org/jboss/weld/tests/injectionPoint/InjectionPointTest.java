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
package org.jboss.weld.tests.injectionPoint;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.ParameterizedType;

import jakarta.enterprise.inject.IllegalProductException;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.InjectionPoint;

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
public class InjectionPointTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InjectionPointTest.class))
                .addPackage(InjectionPointTest.class.getPackage())
                .addClass(Utils.class);
    }

    /*
     * description = "WELD-239"
     */
    @Test
    public void testCorrectInjectionPointUsed(IntConsumer intConsumer, DoubleConsumer doubleConsumer) {
        intConsumer.ping();

        try {
            doubleConsumer.ping();
        } catch (IllegalProductException e) {
            Assert.assertTrue(e.getMessage()
                    .contains("Injection Point: field org.jboss.weld.tests.injectionPoint.DoubleGenerator.timer"));
        }
    }

    /*
     * description = "WELD-316"
     */
    @Test
    public void testFieldInjectionPointSerializability(Consumer consumer) throws Throwable {
        consumer.ping();
        InjectionPoint ip = StringGenerator.getInjectionPoint();
        Assert.assertNotNull(ip);
        Assert.assertEquals("str", ip.getMember().getName());
        InjectionPoint ip1 = Utils.deserialize(Utils.serialize(ip));
        Assert.assertEquals("str", ip1.getMember().getName());
    }

    @Test
    public void testConstructorInjectionPointSerializability(Consumer consumer) throws Throwable {
        InjectionPoint ip = consumer.getSheep().getIp();
        assertNotNull(ip);
        Utils.deserialize(Utils.serialize(ip));
    }

    @Test
    public void testMethodInjectionPointSerializability(Consumer consumer) throws Throwable {
        InjectionPoint ip = consumer.getInitializerSheep().getIp();
        assertNotNull(ip);
        Utils.deserialize(Utils.serialize(ip));
    }

    /*
     * description = "WELD-812"
     */
    @Test
    public void testSerializabilityOfInstance(Estate estate) throws Throwable {
        // We have to perform some indirection to make sure we are fully inside Weld Injection Points, not third party ones!
        Instance<Farm> farm = estate.getFarm();
        farm.get().ping();
        Farm farm1 = Utils.deserialize(Utils.serialize(farm.get()));
        assertNotNull(farm1);
        farm1.ping();
        assertNotNull(farm1.getInjectionPoint());
    }

    @Test
    public void testGetDeclaringType(GrassyField field) {
        Assert.assertEquals("daisy", field.getCow().getName());
    }

    /*
     * description = "WELD-438"
     */
    @Test
    public void testInjectionPointWhenInstanceGetIsUsed(PigSty pigSty) throws Exception {
        Pig pig = pigSty.getPig();
        Assert.assertNotNull(pig);
        Assert.assertNotNull(pig.getInjectionPoint().getBean());
        Assert.assertEquals(PigSty.class, pig.getInjectionPoint().getBean().getBeanClass());
        Assert.assertEquals(PigSty.class.getDeclaredField("pig"), pig.getInjectionPoint().getMember());
        Assert.assertNotNull(pig.getInjectionPoint().getAnnotated());
        Assert.assertTrue(pig.getInjectionPoint().getAnnotated().getBaseType() instanceof ParameterizedType);
        ParameterizedType parameterizedType = ((ParameterizedType) pig.getInjectionPoint().getAnnotated().getBaseType());
        Assert.assertEquals(Instance.class, parameterizedType.getRawType());
        Assert.assertEquals(1, parameterizedType.getActualTypeArguments().length);
        Assert.assertEquals(Pig.class, parameterizedType.getActualTypeArguments()[0]);
        Assert.assertTrue(pig.getInjectionPoint().getAnnotated().isAnnotationPresent(Special.class));
        Assert.assertFalse(pig.getInjectionPoint().getAnnotated().isAnnotationPresent(ExtraSpecial.class));
        Assert.assertTrue(Utils.annotationSetMatches(pig.injectionPoint.getQualifiers(), Special.class, ExtraSpecial.class));
        Assert.assertEquals(Pig.class, pig.getInjectionPoint().getType());
    }
}

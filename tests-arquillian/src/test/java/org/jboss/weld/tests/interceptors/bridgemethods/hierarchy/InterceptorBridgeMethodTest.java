/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.interceptors.bridgemethods.hierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-1672
 * @see WELD-2414
 */
@RunWith(Arquillian.class)
public class InterceptorBridgeMethodTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap
                .create(BeanArchive.class,
                        Utils.getDeploymentNameAsHash(InterceptorBridgeMethodTest.class))
                .intercept(MissileInterceptor.class)
                .addPackage(InterceptorBridgeMethodTest.class.getPackage())
                .addClass(ActionSequence.class);
    }

    @Test
    public void testChild(@Juicy Child child) {
        // Child extends Parent<String> implements Base<T>
        reset();
        child.invokeA("foo");
        verify(Child.class);
        reset();
        child.getA();
        verify(Parent.class);
        reset();
        child.invokeB("foo");
        verify(Parent.class);
        reset();
        child.getB();
        verify(Parent.class);
        reset();
        child.invokeDefault("foo");
        verify(Parent.class);

        reset();
        Parent<String> parent = child;
        parent.invokeA("foo");
        verify(Child.class);
    }

    @Test
    public void testJuicyBase(@Juicy Base<String> base) {
        // Child gets injected
        reset();
        base.invokeA("foo");
        verify(Child.class);
        reset();
        base.getA();
        verify(Parent.class);
        reset();
        base.invokeB("foo");
        verify(Parent.class);
        reset();
        base.getB();
        verify(Parent.class);
        reset();
        base.invokeDefault("foo");
        verify(Parent.class);

    }

    @Test
    public void testSpecialBase(SpecialBase special) {
        // SpecialChild gets injected
        reset();
        special.getA();
        verify(SpecialChild.class);
        reset();
        special.invokeB("foo");
        verify(SpecialChild.class);
        reset();
        special.getB();
        verify(SpecialParent.class);
        reset();
        special.invokeDefault("foo");
        verify(Base.class);
    }

    @Test
    @Ignore("WELD-2424")
    public void testSpecialBaseInvokeA(SpecialBase special) {
        // SpecialChild gets injected
        reset();
        special.invokeA("foo");
        verify(SpecialParent.class);
    }

    @Test
    public void testSpecialChild(SpecialChild child) {
        // SpecialChild extends AbstractParent<String> implements SpecialBase
        reset();
        child.invokeA("foo");
        verify(SpecialParent.class);
        reset();
        child.getA();
        verify(SpecialChild.class);
        reset();
        child.invokeB("foo");
        verify(SpecialChild.class);
        reset();
        child.getB();
        verify(SpecialParent.class);
        reset();
        child.invokeDefault("foo");
        verify(Base.class);
    }

    @Test
    public void testAbstractParent(SpecialParent<String> parent) {
        // SpecialChild gets injected
        reset();
        parent.invokeA("foo");
        verify(SpecialParent.class);
        reset();
        parent.getA();
        verify(SpecialChild.class);
        reset();
        parent.invokeB("foo");
        verify(SpecialChild.class);
        reset();
        parent.getB();
        verify(SpecialParent.class);
        reset();
        parent.invokeDefault("foo");
        verify(Base.class);
    }

    private void reset() {
        MissileInterceptor.INTERCEPTED.set(false);
        ActionSequence.reset();
    }

    private void verify(Class<?> expectedClazz) {
        assertTrue(MissileInterceptor.INTERCEPTED.get());
        assertEquals(1, ActionSequence.getSequenceSize());
        assertEquals(expectedClazz.getName(), ActionSequence.getSequenceData().get(0));
    }

}

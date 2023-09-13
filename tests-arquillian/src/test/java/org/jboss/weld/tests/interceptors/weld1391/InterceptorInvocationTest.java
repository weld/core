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
package org.jboss.weld.tests.interceptors.weld1391;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.Instance;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see https://issues.jboss.org/browse/WELD-1391
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class InterceptorInvocationTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptorInvocationTest.class))
                .intercept(SimpleInterceptor.class)
                .addPackage(InterceptorInvocationTest.class.getPackage()).addClass(ActionSequence.class);
    }

    @Test
    public void testInterceptorInvocation(Instance<SimpleBean> instance) {
        List<String> expected = new ArrayList<String>();
        expected.add("aroundConstruct");
        expected.add("constructor");
        expected.add("postConstruct");
        expected.add("init");
        expected.add("aroundInvoke");
        expected.add("foo");
        expected.add("preDestroy");
        expected.add("destroy");

        ActionSequence.reset();
        SimpleBean bean = instance.get();
        bean.foo();
        instance.destroy(bean);

        assertEquals(expected, ActionSequence.getSequence().getData());
    }
}

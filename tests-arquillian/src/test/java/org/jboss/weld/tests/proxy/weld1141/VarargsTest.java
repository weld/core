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

package org.jboss.weld.tests.proxy.weld1141;

import java.lang.reflect.Method;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class VarargsTest {
    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(VarargsTest.class)).addClass(ArgsBean.class);
    }

    @Inject
    ArgsBean bean;

    @Test
    public void testVarargs() throws Exception {
        Assert.assertTrue(getVarargsMethod(ArgsBean.class).isVarArgs());
        Assert.assertNotNull(bean);
        Assert.assertTrue("Should be varargs method.", getVarargsMethod(bean.getClass()).isVarArgs());
    }

    private Method getVarargsMethod(Class<?> clazz) throws Exception {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals("varargsMethod")) {
                return m;
            }
        }
        throw new IllegalArgumentException("No such method found!");
    }
}

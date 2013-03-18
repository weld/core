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

package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.notfired;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Named;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Named(ProgrammaticallyAddedProcessBeanAttributesTest.IGNORE_ME)
public class ProgrammaticallyAddedProcessBeanAttributesTest {

    public static final String IGNORE_ME = "ignoreMe";

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class)
                .addClasses(Foo.class, MyExtension.class)
                .addAsServiceProvider(Extension.class, MyExtension.class);
    }

    @Test
    public void testProcessBeanAttributesIsNotFiredForProgrammaticallyAddedOrBuiltInBeans() {
        // if PBA is fired for any built-in beans, this will be > 0
        // also if PBA is fired for FooBean, which is added programmatically, this will also be > 0
        Assert.assertEquals(0, MyExtension.processBeanAttributesInvocationCount);
    }
}

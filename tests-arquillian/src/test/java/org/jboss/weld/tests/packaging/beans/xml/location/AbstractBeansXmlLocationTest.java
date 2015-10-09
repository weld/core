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
package org.jboss.weld.tests.packaging.beans.xml.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.test.util.Utils;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public abstract class AbstractBeansXmlLocationTest {

    @Inject
    private InjectedBean bean;

    public static WebArchive getBaseDeployment(Class<?> testClass) {
        return ShrinkWrap.create(WebArchive.class, Utils.getDeploymentNameAsHash(testClass, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(Foo.class, Bar.class, InjectedBean.class, AbstractBeansXmlLocationTest.class);
    }

    public static Asset getBeansXml() {
        BeansXml beans = new BeansXml();
        beans.alternatives(Bar.class);
        return beans;
    }

    @Test
    public void testBeansXmlInUse() {
        assertTrue(bean.getFoo() instanceof Bar);
        assertEquals("bar", bean.getFoo().getName());
    }
}

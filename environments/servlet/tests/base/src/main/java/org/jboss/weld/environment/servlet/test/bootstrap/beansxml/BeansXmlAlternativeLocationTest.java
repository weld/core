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

package org.jboss.weld.environment.servlet.test.bootstrap.beansxml;

import static org.junit.Assert.assertNotNull;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The <code>WEB-INF/classes</code> directory of a war is a bean archive if there is a file named beans.xml in the <code>WEB-INF/classes/META-INF</code>
 * directory of the war.
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class BeansXmlAlternativeLocationTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addClass(BeansXmlAlternativeLocationTest.class)
                .add(new BeansXml(BeanDiscoveryMode.ALL), "WEB-INF/classes/META-INF/beans.xml");
    }

    @Test
    public void testAlternativeLocation(BeanManager beanManager) {
        assertNotNull(beanManager);
    }

}

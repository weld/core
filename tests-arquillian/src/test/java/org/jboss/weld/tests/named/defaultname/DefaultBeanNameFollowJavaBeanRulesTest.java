/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.named.defaultname;

import static org.junit.Assert.assertEquals;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class DefaultBeanNameFollowJavaBeanRulesTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(DefaultBeanNameFollowJavaBeanRulesTest.class))
                .addPackage(DefaultBeanNameFollowJavaBeanRulesTest.class.getPackage())
                .addAsResource(PropertiesBuilder.newBuilder().set(ConfigurationKey.DEFAULT_BEAN_NAMES_FOLLOW_JAVABEAN_RULES.get(), "true").build(),
                        "weld.properties");
    }

    @Test
    public void testDefaultNames(BeanManager beanManager) {
        assertEquals(1, beanManager.getBeans("URLAlpha").size());
        assertEquals(1, beanManager.getBeans("bravo").size());
        assertEquals(1, beanManager.getBeans("alpha").size());
    }

}

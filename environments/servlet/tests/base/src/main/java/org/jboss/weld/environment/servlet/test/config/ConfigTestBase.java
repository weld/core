/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.servlet.test.config;

import java.util.ArrayList;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.shrinkwrap.impl.BeansXml.Exclude;
import org.jboss.weld.environment.servlet.test.config.dos.DOSBean;
import org.jboss.weld.environment.servlet.test.util.Deployments;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class ConfigTestBase {

    public static WebArchive baseDeployment(final Package... excludedPackages) {
        // BeanDiscoveryMode.ALL because many tests have 0 beans to discover and Weld would just skip initialization
        BeansXml beansXml = new BeansXml(BeanDiscoveryMode.ALL);
        ArrayList<Exclude> filters = new ArrayList<Exclude>();
        for (Package pckg : excludedPackages) {
            filters.add(Exclude.match(pckg.getName() + ".**"));
        }
        beansXml.excludeFilters(filters.toArray(new Exclude[filters.size()]));
        return Deployments.baseDeployment(beansXml);
    }

    @Inject
    private BeanManager beanManager;

    protected void assertBeans(Class<?> beanClass, int size) {
        Assert.assertNotNull("Null bean manager", beanManager);
        Set<Bean<?>> beans = beanManager.getBeans(beanClass);
        Assert.assertEquals(size, beans.size());
    }

    @Test
    public void testDOS() throws Exception {
        assertBeans(GoodBean.class, 1);
        assertBeans(DOSBean.class, 0);
    }
}

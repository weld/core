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

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.config.dos.DOSBean;
import org.jboss.weld.environment.servlet.test.util.BeansXml;
import org.jboss.weld.environment.servlet.test.util.Deployments;
import org.junit.Assert;
import org.junit.Test;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.Set;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ConfigTestBase {

    public static WebArchive baseDeployment(final Package... excludedPackages) {
        BeansXml beansXml = new BeansXml() {
            protected void appendExternal(StringBuilder xml) {
                xml.append("<weld:scan>");
                for (Package pckg : excludedPackages)
                    xml.append("<weld:exclude name=\"").append(pckg.getName()).append(".**\"/>");
                xml.append("</weld:scan>");
            }
        };
        beansXml.setSchema(BeansXml.FULL_SCHEMA);
        return Deployments.baseDeployment(beansXml);
    }

    public static WebArchive baseDOSDeployment() {
        return baseDeployment(DOSBean.class.getPackage()).addClasses(ConfigTestBase.class, GoodBean.class, DOSBean.class);
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

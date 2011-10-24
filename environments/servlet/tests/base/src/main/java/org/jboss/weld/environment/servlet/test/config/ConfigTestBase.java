/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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

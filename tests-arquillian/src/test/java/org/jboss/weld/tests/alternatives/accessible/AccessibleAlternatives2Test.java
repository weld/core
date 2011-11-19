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

package org.jboss.weld.tests.alternatives.accessible;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class AccessibleAlternatives2Test {
    @Deployment
    public static Archive<?> deploy() {
        JavaArchive lib = ShrinkWrap.create(BeanArchive.class, "test.jar").addClasses(IUser.class, AUser.class);
        StringAsset beansXml = new StringAsset("<beans><alternatives><class>" + BUser.class.getName()  + "</class></alternatives></beans>");
        return ShrinkWrap.create(WebArchive.class)
                .addAsLibrary(lib)
                .addAsWebInfResource(beansXml, "beans.xml")
                .addClasses(BUser.class);
    }

    @Inject
    private BeanManager beanManager;

    @Test
    public void testAlternatives() {
        Assert.assertEquals(BUser.class, beanManager.resolve(beanManager.getBeans(IUser.class)).getBeanClass());
    }
}

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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.environment.servlet.test.util.BeansXml;
import org.jboss.weld.environment.servlet.test.util.Deployments;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:csadilek@redhat.com">Christian Sadilek</a>
 */
@RunWith(Arquillian.class)
public class ConfigWithoutJettyEnvTest extends ConfigTestBase {

    public static final String DEFAULT_WEB_XML_START = "<web-app>";
    public static final String DEFAULT_WEB_XML_BODY = 
        Deployments.toListener("org.jboss.weld.environment.servlet.Listener") + 
        Deployments.toListener("org.jboss.weld.environment.servlet.BeanManagerResourceBindingListener");
    
    public static final String DEFAULT_WEB_XML_PREFIX = DEFAULT_WEB_XML_START + DEFAULT_WEB_XML_BODY;
    public static final String DEFAULT_WEB_XML_SUFFIX = "</web-app>";

    public static final Asset WEB_XML = 
        new ByteArrayAsset((DEFAULT_WEB_XML_PREFIX + DEFAULT_WEB_XML_SUFFIX).getBytes());

    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addAsWebInfResource(new BeansXml(), "beans.xml")
            .setWebXML(WEB_XML)
            .addClass(GoodBean.class);
    }
}
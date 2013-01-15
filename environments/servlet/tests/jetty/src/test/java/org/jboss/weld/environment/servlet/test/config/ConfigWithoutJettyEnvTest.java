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
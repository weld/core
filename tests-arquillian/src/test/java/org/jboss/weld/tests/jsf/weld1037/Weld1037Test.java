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

package org.jboss.weld.tests.jsf.weld1037;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import jakarta.servlet.http.HttpServletResponse;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class Weld1037Test {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, Utils.getDeploymentNameAsHash(Weld1037Test.class, Utils.ARCHIVE_TYPE.WAR))
                .addClass(RedirectBean.class)
                .addAsWebResource(Weld1037Test.class.getPackage(), "doRedirect.xhtml", "doRedirect.xhtml")
                .addAsWebInfResource(Weld1037Test.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(Weld1037Test.class.getPackage(), "faces-config.xml", "faces-config.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @RunAsClient
    public void testRedirectInPreRenderViewAction(@ArquillianResource URL url) throws Exception {
        WebClient client = new WebClient();
        WebClientOptions options = client.getOptions();
        options.setRedirectEnabled(false);
        options.setThrowExceptionOnFailingStatusCode(false);

        Page page = client.getPage(url + "/doRedirect.faces");
        assertEquals("Expected redirect:", HttpServletResponse.SC_MOVED_TEMPORARILY, page.getWebResponse().getStatusCode());
    }

}

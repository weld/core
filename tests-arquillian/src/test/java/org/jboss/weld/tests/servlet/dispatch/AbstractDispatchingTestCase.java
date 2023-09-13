/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.servlet.dispatch;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

public abstract class AbstractDispatchingTestCase {

    @ArquillianResource(MainServlet.class)
    protected URL contextPath;

    protected final WebClient client = new WebClient();

    @Before
    public void reset() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        client.getPage(contextPath + "main/reset");
    }

    @Test
    public void testLocalInclude() throws IOException {
        assertEquals("first:first", getResponseAsString("main/dispatch/include"));
        assertEquals("true", getResponseAsString("main/validate"));
    }

    @Test
    public void testLocalForward(@ArquillianResource(MainServlet.class) URL contextPath) throws IOException {
        assertEquals("first:first", getResponseAsString("main/dispatch/forward"));
        assertEquals("true", getResponseAsString("main/validate"));
    }

    @Test
    public void testCrossContextInclude(@ArquillianResource(MainServlet.class) URL contextPath) throws IOException {
        assertEquals("second:second", getResponseAsString("main/dispatch/include?crossContext=true"));
        assertEquals("true", getResponseAsString("main/validate"));
    }

    @Test
    public void testCrossContextForward(@ArquillianResource(MainServlet.class) URL contextPath) throws IOException {
        assertEquals("second:second", getResponseAsString("main/dispatch/forward?crossContext=true"));
        assertEquals("true", getResponseAsString("main/validate"));
    }

    private String getResponseAsString(String urlSuffix)
            throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        Page page = client.getPage(contextPath + urlSuffix);
        assertEquals(200, page.getWebResponse().getStatusCode());
        return page.getWebResponse().getContentAsString();
    }
}

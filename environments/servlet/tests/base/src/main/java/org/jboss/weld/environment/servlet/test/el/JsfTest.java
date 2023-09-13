/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.test.el;

import static org.jboss.weld.environment.servlet.test.util.Deployments.EMPTY_FACES_CONFIG_XML;
import static org.jboss.weld.environment.servlet.test.util.Deployments.FACES_WEB_XML;
import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;

@RunAsClient
@RunWith(Arquillian.class)
public class JsfTest {

    public static final Asset CHARLIE_XHTML = new ByteArrayAsset(("<jsp:root xmlns:jsp=\"http://java.sun.com/JSP/Page\" " +
            "xmlns:h=\"http://java.sun.com/jsf/html\" " +
            "xmlns:f=\"http://java.sun.com/jsf/core\" " +
            "xmlns:s=\"http://jboss.com/products/seam/taglib\" " +
            "xmlns=\"http://www.w3.org/1999/xhtml\" " +
            "version=\"2.0\"> " +
            "<jsp:output doctype-root-element=\"html\" " +
            "doctype-public=\"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
            "doctype-system=\"http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"/> " +
            "<jsp:directive.page contentType=\"text/html\"/> " +
            "<html> " +
            "<head /> " +
            "<body> " +
            "<f:view> " +
            "<h:outputText value=\"#{chicken.name}\" id=\"oldel\"/>" +
            "<h:outputText value=\"#{chicken.getName()}\" id=\"newel\"/>" +
            "</f:view> " +
            "</body> " +
            "</html> " +
            "</jsp:root>").getBytes());

    @Deployment
    public static WebArchive createTestArchive() {
        return baseDeployment(FACES_WEB_XML)
                .add(CHARLIE_XHTML, "charlie.xhtml")
                .addAsWebInfResource(EMPTY_FACES_CONFIG_XML, "faces-config.xml")
                .addClass(Chicken.class);
    }

    @Test
    public void testELWithParameters(@ArquillianResource URL baseURL) throws Exception {
        WebClient client = new WebClient();
        HtmlPage page = client.getPage(new URL(baseURL, "charlie.jsf"));

        page.asXml();

        HtmlSpan oldel = getFirstMatchingElement(page, HtmlSpan.class, "oldel");
        assertNotNull(oldel);
        final String charlie = "Charlie";
        assertEquals(charlie, oldel.asNormalizedText());

        HtmlSpan newel = getFirstMatchingElement(page, HtmlSpan.class, "newel");
        assertNotNull(newel);
        assertEquals(charlie, newel.asNormalizedText());
    }

    protected <T> Set<T> getElements(HtmlElement rootElement, Class<T> elementClass) {
        Set<T> result = new HashSet<T>();

        for (HtmlElement element : rootElement.getHtmlElementDescendants()) {
            result.addAll(getElements(element, elementClass));
        }

        if (elementClass.isInstance(rootElement)) {
            result.add(elementClass.cast(rootElement));
        }
        return result;

    }

    protected <T extends HtmlElement> T getFirstMatchingElement(HtmlPage page, Class<T> elementClass, String id) {

        Set<T> inputs = getElements(page.getBody(), elementClass);
        for (T input : inputs) {
            if (input.getId().contains(id)) {
                return input;
            }
        }
        return null;
    }

}

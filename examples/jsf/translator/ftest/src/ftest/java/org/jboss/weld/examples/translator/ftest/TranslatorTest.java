/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.examples.translator.ftest;

import java.net.URL;

import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.locator.IdLocator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.arquillian.ajocado.locator.LocatorFactory.id;
import static org.jboss.arquillian.ajocado.Ajocado.elementPresent;
import static org.jboss.arquillian.ajocado.Ajocado.waitForHttp;
import static org.jboss.arquillian.ajocado.Ajocado.waitModel;
import static org.junit.Assert.assertTrue;

/**
 * Tests translator example in Weld
 *
 * @author maschmid
 * @author mgencur
 * @author plenyi
 */
@RunWith(Arquillian.class)
@RunAsClient
public class TranslatorTest {
    protected String MAIN_PAGE = "/home.jsf";
    protected IdLocator INPUT_AREA = id("TranslatorMain:text");
    protected IdLocator TRANSLATE_BUTTON = id("TranslatorMain:button");
    protected String ONE_SENTENCE = "This is only one sentence.";
    protected String MORE_SENTENCES = "First sentence. Second and last sentence.";
    protected String ONE_SENTENCE_TRANSLATED = "Lorem ipsum dolor sit amet.";
    protected String MORE_SENTENCES_TRANSLATED = "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.";

    @Drone
    AjaxSelenium selenium;
    
    @ArquillianResource
    private URL contextPath;
    
    @Deployment
    public static EnterpriseArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }
    
    @Before
    public void openStartUrl() {
        selenium.open(contextPath);
        waitModel.until(elementPresent.locator(INPUT_AREA));
    }

    @Test
    public void translateTest() {
        selenium.type(INPUT_AREA, ONE_SENTENCE);
        waitForHttp(selenium).click(TRANSLATE_BUTTON);
        assertTrue("One sentence translated into latin expected.", selenium.isTextPresent(ONE_SENTENCE_TRANSLATED));
        selenium.type(INPUT_AREA, MORE_SENTENCES);
        waitForHttp(selenium).click(TRANSLATE_BUTTON);
        assertTrue("More sentences translated into latin expected.", selenium.isTextPresent(MORE_SENTENCES_TRANSLATED));
    }
}

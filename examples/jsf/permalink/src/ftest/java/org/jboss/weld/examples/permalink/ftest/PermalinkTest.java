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
package org.jboss.weld.examples.permalink.ftest;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.locator.IdLocator;
import org.jboss.arquillian.ajocado.locator.XPathLocator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.arquillian.ajocado.locator.LocatorFactory.id;
import static org.jboss.arquillian.ajocado.locator.LocatorFactory.xp;
import static org.jboss.arquillian.ajocado.Ajocado.elementPresent;
import static org.jboss.arquillian.ajocado.Ajocado.waitForHttp;
import static org.jboss.arquillian.ajocado.Ajocado.waitModel;
import static org.junit.Assert.assertTrue;

/**
 * Tests permalink example in Weld. The first test just adds comment on the first
 * topic and returns back. Second test tests permanent link from jsf2.
 *
 * @author maschmid
 * @author mgencur
 * @author plenyi
 */
@RunWith(Arquillian.class)
@RunAsClient
public class PermalinkTest {
    protected String MAIN_PAGE = "/home.jsf";
    protected String PAGE_TITLE = "Direct links to the news you crave";
    protected XPathLocator VIEW_ENTRY_LINK = xp("//a[contains(text(),'View Entry')][1]");
    protected String TOPIC_TITLE = "Mojarra == RI";
    protected XPathLocator PERMALINK_LINK = xp("//a[@title='A bookmarkable link for this entry.'][1]");
    protected IdLocator AUTHOR_INPUT = id("author");
    protected IdLocator COMMENT_INPUT = id("body");
    protected IdLocator SUBMIT_BUTTON = id("post");
    protected XPathLocator BACK_BUTTON = xp("//input[@type='button'][@value='Back to main page']");
    protected String COMMENT_TEXT = "This is my first comment on Mojarra project";
    protected String AUTHOR_NAME = "Martin";
    protected String TEXT_ON_HOME_PAGE = "Annotation nation";

    @Drone
    AjaxSelenium selenium;
    
    @ArquillianResource
    private URL contextPath;
    
    @Deployment(testable = false)
    public static WebArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }
    
    @Before
    public void openStartUrl() throws MalformedURLException {
        selenium.open(new URL(contextPath.toString() + "home.jsf"));
        waitModel.until(elementPresent.locator(VIEW_ENTRY_LINK));
    }   

    @Test
    @InSequence(1)
    public void addCommentOnTopicTest() {
        waitForHttp(selenium).click(VIEW_ENTRY_LINK);
        assertTrue("Topic title expected on the page", selenium.isTextPresent(TOPIC_TITLE));
        selenium.type(AUTHOR_INPUT, AUTHOR_NAME);
        selenium.type(COMMENT_INPUT, COMMENT_TEXT);
        waitForHttp(selenium).click(SUBMIT_BUTTON);
        assertTrue("A name of comment's author expected", selenium.isTextPresent(AUTHOR_NAME));
        assertTrue("A text of entered comment expected", selenium.isTextPresent(COMMENT_TEXT));
        waitForHttp(selenium).click(BACK_BUTTON);
        assertTrue("Home page expected", selenium.isTextPresent(TEXT_ON_HOME_PAGE));
    }

    @Test
    @InSequence(2)
    public void permanentLinkTest() {
        waitForHttp(selenium).click(PERMALINK_LINK);
        assertTrue("A name of comment's author expected", selenium.isTextPresent(AUTHOR_NAME));
        assertTrue("A text of entered comment expected", selenium.isTextPresent(COMMENT_TEXT));
    }
}
